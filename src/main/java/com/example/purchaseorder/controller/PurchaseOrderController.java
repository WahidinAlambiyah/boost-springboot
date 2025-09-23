package com.example.purchaseorder.controller;

import com.example.purchaseorder.domain.PurchaseOrderHeader;
import com.example.purchaseorder.dto.PurchaseOrderRequest;
import com.example.purchaseorder.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<PurchaseOrderHeader> create(@RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.ok(purchaseOrderService.create(request));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<PurchaseOrderHeader>> createBulk(@RequestBody List<PurchaseOrderRequest> requests) {
        return ResponseEntity.ok(purchaseOrderService.createBulk(requests));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderHeader> update(@PathVariable Long id, @RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.ok(purchaseOrderService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderHeader> get(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.get(id));
    }

    @GetMapping
    public ResponseEntity<Page<PurchaseOrderHeader>> list(Pageable pageable) {
        return ResponseEntity.ok(purchaseOrderService.list(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        purchaseOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
