package com.example.boost.billing.application;

import com.example.boost.billing.infrastructure.CoachPayrollItemRepository;
import com.example.boost.billing.infrastructure.CoachPayrollPeriodRepository;
import com.example.boost.catalog.infrastructure.AcademyRepository;
import com.example.boost.catalog.infrastructure.CoachProfileRepository;
import com.example.boost.common.exception.BadRequestException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.dto.*;
import com.example.boost.domain.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoachPayrollService {
    private static final String DRAFT = "DRAFT";
    private static final String CALCULATED = "CALCULATED";
    private static final String APPROVED = "APPROVED";
    private static final String PAID = "PAID";

    private final CoachPayrollPeriodRepository periodRepository;
    private final CoachPayrollItemRepository itemRepository;
    private final CoachProfileRepository coachProfileRepository;
    private final AcademyRepository academyRepository;

    @Transactional
    public PayrollResponse generate(PayrollGenerateRequest request) {
        Academy academy = academyRepository.findById(request.academyId()).orElseThrow(() -> new NotFoundException("Academy not found"));
        CoachPayrollPeriod period = periodRepository
                .findByAcademyIdAndPeriodMonthAndPeriodYearAndDeletedAtIsNull(request.academyId(), request.periodMonth(), request.periodYear())
                .orElseGet(() -> {
                    CoachPayrollPeriod created = new CoachPayrollPeriod();
                    created.setAcademy(academy);
                    created.setPeriodMonth(request.periodMonth());
                    created.setPeriodYear(request.periodYear());
                    created.setStatus(DRAFT);
                    return periodRepository.save(created);
                });

        List<CoachProfile> coaches = coachProfileRepository.findByAcademyIdAndIsActiveTrueAndDeletedAtIsNull(request.academyId());
        Map<UUID, CoachPayrollItemRepository.CoachMonthlySessionAggregate> aggregateMap = itemRepository
                .aggregateCoachMonthlySessions(request.academyId(), request.periodMonth(), request.periodYear())
                .stream().collect(Collectors.toMap(CoachPayrollItemRepository.CoachMonthlySessionAggregate::getCoachId, Function.identity()));

        Map<UUID, CoachPayrollItem> existing = itemRepository.findByPayrollPeriodIdAndDeletedAtIsNullOrderByCreatedAtDesc(period.getId())
                .stream().collect(Collectors.toMap(i -> i.getCoach().getId(), Function.identity(), (a, b) -> a));

        for (CoachProfile coach : coaches) {
            CoachPayrollItem item = existing.getOrDefault(coach.getId(), new CoachPayrollItem());
            item.setPayrollPeriod(period);
            item.setCoach(coach);
            item.setBaseAmount(Optional.ofNullable(coach.getMonthlySalary()).orElse(BigDecimal.ZERO));
            if (item.getBonusAmount() == null) item.setBonusAmount(BigDecimal.ZERO);
            if (item.getDeductionAmount() == null) item.setDeductionAmount(BigDecimal.ZERO);
            // aggregate accessed for future extension/audit requirements
            aggregateMap.get(coach.getId());
            item.setTotalAmount(item.getBaseAmount().add(item.getBonusAmount()).subtract(item.getDeductionAmount()));
            itemRepository.save(item);
        }

        period.setStatus(CALCULATED);
        period.setGeneratedAt(OffsetDateTime.now());
        periodRepository.save(period);
        return toResponse(period);
    }

    @Transactional
    public PayrollResponse approve(UUID payrollPeriodId) {
        CoachPayrollPeriod period = getPeriod(payrollPeriodId);
        if (!Set.of(DRAFT, CALCULATED).contains(period.getStatus())) throw new BadRequestException("Only DRAFT/CALCULATED can be approved");
        period.setStatus(APPROVED);
        period.setApprovedAt(OffsetDateTime.now());
        return toResponse(periodRepository.save(period));
    }

    @Transactional(readOnly = true)
    public List<PayrollResponse> findByMonthAndYear(Integer month, Integer year) {
        return periodRepository.findByPeriodMonthAndPeriodYearAndDeletedAtIsNullOrderByCreatedAtDesc(month, year)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PayrollResponse findById(UUID payrollPeriodId) {
        return toResponse(getPeriod(payrollPeriodId));
    }

    @Transactional
    public PayrollResponse markPaid(UUID payrollPeriodId) {
        CoachPayrollPeriod period = getPeriod(payrollPeriodId);
        if (!APPROVED.equals(period.getStatus())) throw new BadRequestException("Only APPROVED period can be paid");
        period.setStatus(PAID);
        period.setPaidAt(OffsetDateTime.now());
        return toResponse(periodRepository.save(period));
    }

    @Transactional
    public PayrollItemResponse updateItem(UUID payrollPeriodId, UUID itemId, PayrollItemUpdateRequest request) {
        CoachPayrollPeriod period = getPeriod(payrollPeriodId);
        if (PAID.equals(period.getStatus())) throw new BadRequestException("Cannot edit paid payroll period");
        CoachPayrollItem item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Payroll item not found"));
        if (!item.getPayrollPeriod().getId().equals(payrollPeriodId)) throw new BadRequestException("Payroll item does not belong to payroll period");
        item.setBonusAmount(request.bonusAmount());
        item.setDeductionAmount(request.deductionAmount());
        item.setTotalAmount(item.getBaseAmount().add(item.getBonusAmount()).subtract(item.getDeductionAmount()));
        return toItemResponse(itemRepository.save(item));
    }

    private CoachPayrollPeriod getPeriod(UUID id) {
        return periodRepository.findById(id).orElseThrow(() -> new NotFoundException("Payroll period not found"));
    }

    private PayrollResponse toResponse(CoachPayrollPeriod period) {
        List<PayrollItemResponse> items = itemRepository.findByPayrollPeriodIdAndDeletedAtIsNullOrderByCreatedAtDesc(period.getId()).stream().map(this::toItemResponse).toList();
        return new PayrollResponse(period.getId(), period.getAcademy().getId(), period.getPeriodMonth(), period.getPeriodYear(), period.getStatus(), period.getGeneratedAt(), period.getApprovedAt(), period.getPaidAt(), items);
    }

    private PayrollItemResponse toItemResponse(CoachPayrollItem item) {
        return new PayrollItemResponse(item.getId(), item.getCoach().getId(), item.getBaseAmount(), item.getBonusAmount(), item.getDeductionAmount(), item.getTotalAmount());
    }
}
