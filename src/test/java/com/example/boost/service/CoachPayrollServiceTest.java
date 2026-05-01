package com.example.boost.service;

import com.example.boost.billing.application.CoachPayrollService;
import com.example.boost.billing.infrastructure.CoachPayrollItemRepository;
import com.example.boost.billing.infrastructure.CoachPayrollPeriodRepository;
import com.example.boost.catalog.infrastructure.AcademyRepository;
import com.example.boost.catalog.infrastructure.CoachProfileRepository;
import com.example.boost.domain.dto.PayrollGenerateRequest;
import com.example.boost.domain.dto.PayrollResponse;
import com.example.boost.domain.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoachPayrollServiceTest {
    @Mock private CoachPayrollPeriodRepository periodRepository;
    @Mock private CoachPayrollItemRepository itemRepository;
    @Mock private CoachProfileRepository coachProfileRepository;
    @Mock private AcademyRepository academyRepository;

    @InjectMocks private CoachPayrollService coachPayrollService;

    @Test
    void generateShouldIncludeOnlyActiveCoachesAndCalculateTotalAmountCorrectly() {
        UUID academyId = UUID.randomUUID();
        UUID periodId = UUID.randomUUID();
        Academy academy = new Academy(); academy.setId(academyId);

        CoachPayrollPeriod period = new CoachPayrollPeriod();
        period.setId(periodId); period.setAcademy(academy); period.setPeriodMonth(4); period.setPeriodYear(2026); period.setStatus("DRAFT");

        CoachProfile activeCoach = new CoachProfile();
        activeCoach.setId(UUID.randomUUID());
        activeCoach.setMonthlySalary(new BigDecimal("3000000.00"));

        when(academyRepository.findById(academyId)).thenReturn(Optional.of(academy));
        when(periodRepository.findByAcademyIdAndPeriodMonthAndPeriodYearAndDeletedAtIsNull(academyId, 4, 2026)).thenReturn(Optional.of(period));
        when(coachProfileRepository.findByAcademyIdAndIsActiveTrueAndDeletedAtIsNull(academyId)).thenReturn(List.of(activeCoach));
        when(itemRepository.aggregateCoachMonthlySessions(academyId, 4, 2026)).thenReturn(List.of());
        AtomicReference<CoachPayrollItem> savedItem = new AtomicReference<>();
        when(itemRepository.findByPayrollPeriodIdAndDeletedAtIsNullOrderByCreatedAtDesc(periodId)).thenAnswer(inv -> {
            CoachPayrollItem item = savedItem.get();
            return item == null ? List.of() : List.of(item);
        });
        when(itemRepository.save(any(CoachPayrollItem.class))).thenAnswer(inv -> { CoachPayrollItem item = inv.getArgument(0); savedItem.set(item); return item; });
        when(periodRepository.save(any(CoachPayrollPeriod.class))).thenAnswer(inv -> inv.getArgument(0));

        PayrollResponse response = coachPayrollService.generate(new PayrollGenerateRequest(academyId, 4, 2026));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).coachId()).isEqualTo(activeCoach.getId());
        assertThat(response.items().get(0).baseAmount()).isEqualByComparingTo("3000000.00");
        assertThat(response.items().get(0).totalAmount()).isEqualByComparingTo("3000000.00");
        assertThat(response.status()).isEqualTo("CALCULATED");
    }
}
