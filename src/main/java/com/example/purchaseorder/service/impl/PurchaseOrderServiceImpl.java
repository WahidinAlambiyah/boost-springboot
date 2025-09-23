package com.example.purchaseorder.service.impl;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.domain.PurchaseOrderDetail;
import com.example.purchaseorder.domain.PurchaseOrderHeader;
import com.example.purchaseorder.dto.PurchaseOrderDetailRequest;
import com.example.purchaseorder.dto.PurchaseOrderRequest;
import com.example.purchaseorder.exception.ResourceNotFoundException;
import com.example.purchaseorder.repository.ItemRepository;
import com.example.purchaseorder.repository.PurchaseOrderHeaderRepository;
import com.example.purchaseorder.service.PurchaseOrderService;
import com.example.purchaseorder.service.util.AuditUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderHeaderRepository headerRepository;
    private final ItemRepository itemRepository;

    @Override
    public PurchaseOrderHeader create(PurchaseOrderRequest request) {
        return createPurchaseOrder(request);
    }

    @Override
    public List<PurchaseOrderHeader> createBulk(List<PurchaseOrderRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }
        return requests.stream()
                .map(this::createPurchaseOrder)
                .collect(Collectors.toList());
    }

    @Override
    public PurchaseOrderHeader update(Long id, PurchaseOrderRequest request) {
        PurchaseOrderHeader header = get(id);
        applyHeader(header, request);
        String auditor = AuditUtils.resolveCurrentAuditor();
        OffsetDateTime now = AuditUtils.currentDateTime();
        applyUpdateAudit(header, auditor, now);
        applyDetails(header, request.getDetails(), auditor, now, true);
        applyTotals(header, request);
        return headerRepository.save(header);
    }

    @Override
    public void delete(Long id) {
        PurchaseOrderHeader header = headerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + id));
        header.setDeleted(true);
        headerRepository.save(header);
    }

    @Override
    public void deletePermanent(Long id) {
        PurchaseOrderHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + id));
        headerRepository.delete(header);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderHeader get(Long id) {
        return headerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrderHeader> list(Pageable pageable) {
        return headerRepository.findAllByDeletedFalse(pageable);
    }

    private void applyHeader(PurchaseOrderHeader header, PurchaseOrderRequest request) {
        header.setDatetime(request.getDatetime());
        header.setDescription(request.getDescription());
    }

    private void applyTotals(PurchaseOrderHeader header, PurchaseOrderRequest request) {
        if (request.getTotalCost() != null) {
            header.setTotalCost(request.getTotalCost());
        } else {
            header.setTotalCost(calculateTotalCost(header));
        }
        if (request.getTotalPrice() != null) {
            header.setTotalPrice(request.getTotalPrice());
        } else {
            header.setTotalPrice(calculateTotalPrice(header));
        }
    }

    private BigDecimal calculateTotalCost(PurchaseOrderHeader header) {
        return header.getDetails().stream()
                .map(d -> d.getItemCost().multiply(BigDecimal.valueOf(d.getItemQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalPrice(PurchaseOrderHeader header) {
        return header.getDetails().stream()
                .map(d -> d.getItemPrice().multiply(BigDecimal.valueOf(d.getItemQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void applyDetails(PurchaseOrderHeader header, List<PurchaseOrderDetailRequest> requests,
                               String auditor, OffsetDateTime timestamp, boolean isUpdate) {
        if (header.getDetails() == null) {
            header.setDetails(new ArrayList<>());
        } else {
            header.getDetails().clear();
        }
        if (Objects.isNull(requests)) {
            return;
        }
        for (PurchaseOrderDetailRequest detailRequest : requests) {
            Item item = itemRepository.findByIdAndDeletedFalse(detailRequest.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + detailRequest.getItemId()));
            PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                    .purchaseOrder(header)
                    .item(item)
                    .itemQty(detailRequest.getItemQty())
                    .itemCost(detailRequest.getItemCost())
                    .itemPrice(detailRequest.getItemPrice())
                    .build();
            applyCreationAudit(detail, auditor, timestamp);
            if (isUpdate) {
                applyUpdateAudit(detail, auditor, timestamp);
            }
            header.getDetails().add(detail);
        }
    }

    private PurchaseOrderHeader createPurchaseOrder(PurchaseOrderRequest request) {
        PurchaseOrderHeader header = new PurchaseOrderHeader();
        applyHeader(header, request);
        String auditor = AuditUtils.resolveCurrentAuditor();
        OffsetDateTime now = AuditUtils.currentDateTime();
        applyCreationAudit(header, auditor, now);
        applyDetails(header, request.getDetails(), auditor, now, false);
        applyTotals(header, request);
        header.setDeleted(false);
        return headerRepository.save(header);
    }

    private void applyCreationAudit(PurchaseOrderHeader header, String auditor, OffsetDateTime timestamp) {
        header.setCreatedBy(auditor);
        header.setCreatedDatetime(timestamp);
        header.setUpdatedBy(null);
        header.setUpdatedDatetime(null);
    }

    private void applyUpdateAudit(PurchaseOrderHeader header, String auditor, OffsetDateTime timestamp) {
        header.setUpdatedBy(auditor);
        header.setUpdatedDatetime(timestamp);
    }

    private void applyCreationAudit(PurchaseOrderDetail detail, String auditor, OffsetDateTime timestamp) {
        detail.setCreatedBy(auditor);
        detail.setCreatedDatetime(timestamp);
        detail.setUpdatedBy(null);
        detail.setUpdatedDatetime(null);
    }

    private void applyUpdateAudit(PurchaseOrderDetail detail, String auditor, OffsetDateTime timestamp) {
        detail.setUpdatedBy(auditor);
        detail.setUpdatedDatetime(timestamp);
    }
}
