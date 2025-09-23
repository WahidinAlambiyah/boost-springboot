package com.example.purchaseorder.service;

import com.example.purchaseorder.domain.PurchaseOrderHeader;
import com.example.purchaseorder.dto.PurchaseOrderRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PurchaseOrderService {
    PurchaseOrderHeader create(PurchaseOrderRequest request);

    List<PurchaseOrderHeader> createBulk(List<PurchaseOrderRequest> requests);

    PurchaseOrderHeader update(Long id, PurchaseOrderRequest request);

    void delete(Long id);

    void deletePermanent(Long id);

    PurchaseOrderHeader get(Long id);

    Page<PurchaseOrderHeader> list(Pageable pageable);
}
