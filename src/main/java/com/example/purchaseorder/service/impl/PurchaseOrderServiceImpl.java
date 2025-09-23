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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        applyDetails(header, request.getDetails());
        applyTotals(header, request);
        return headerRepository.save(header);
    }

    @Override
    public void delete(Long id) {
        headerRepository.delete(get(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderHeader get(Long id) {
        return headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrderHeader> list(Pageable pageable) {
        return headerRepository.findAll(pageable);
    }

    private void applyHeader(PurchaseOrderHeader header, PurchaseOrderRequest request) {
        header.setDatetime(request.getDatetime());
        header.setDescription(request.getDescription());
        header.setCreatedBy(request.getCreatedBy());
        header.setCreatedDatetime(request.getCreatedDatetime());
        header.setUpdatedBy(request.getUpdatedBy());
        header.setUpdatedDatetime(request.getUpdatedDatetime());
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

    private void applyDetails(PurchaseOrderHeader header, List<PurchaseOrderDetailRequest> requests) {
        header.getDetails().clear();
        if (Objects.isNull(requests)) {
            return;
        }
        for (PurchaseOrderDetailRequest detailRequest : requests) {
            Item item = itemRepository.findById(detailRequest.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + detailRequest.getItemId()));
            PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                    .purchaseOrder(header)
                    .item(item)
                    .itemQty(detailRequest.getItemQty())
                    .itemCost(detailRequest.getItemCost())
                    .itemPrice(detailRequest.getItemPrice())
                    .createdBy(detailRequest.getCreatedBy())
                    .createdDatetime(detailRequest.getCreatedDatetime())
                    .updatedBy(detailRequest.getUpdatedBy())
                    .updatedDatetime(detailRequest.getUpdatedDatetime())
                    .build();
            header.getDetails().add(detail);
        }
    }

    private PurchaseOrderHeader createPurchaseOrder(PurchaseOrderRequest request) {
        PurchaseOrderHeader header = new PurchaseOrderHeader();
        applyHeader(header, request);
        applyDetails(header, request.getDetails());
        applyTotals(header, request);
        return headerRepository.save(header);
    }
}
