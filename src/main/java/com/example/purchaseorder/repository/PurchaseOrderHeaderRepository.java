package com.example.purchaseorder.repository;

import com.example.purchaseorder.domain.PurchaseOrderHeader;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderHeaderRepository extends JpaRepository<PurchaseOrderHeader, Long> {
}
