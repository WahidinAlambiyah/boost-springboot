package com.example.purchaseorder.service;

import com.example.purchaseorder.domain.PurchaseOrderHeader;
import com.example.purchaseorder.dto.PurchaseOrderRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PurchaseOrderService {
    PurchaseOrderHeader create(PurchaseOrderRequest request);

    PurchaseOrderHeader update(Long id, PurchaseOrderRequest request);

    void delete(Long id);

    PurchaseOrderHeader get(Long id);

    Page<PurchaseOrderHeader> list(Pageable pageable);
}
