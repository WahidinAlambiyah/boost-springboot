package com.example.purchaseorder.repository;

import com.example.purchaseorder.domain.PurchaseOrderHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseOrderHeaderRepository extends JpaRepository<PurchaseOrderHeader, Long> {

    Optional<PurchaseOrderHeader> findByIdAndDeletedFalse(Long id);

    Page<PurchaseOrderHeader> findAllByDeletedFalse(Pageable pageable);
}
