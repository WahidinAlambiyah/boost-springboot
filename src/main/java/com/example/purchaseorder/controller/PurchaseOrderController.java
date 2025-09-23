package com.example.purchaseorder.controller;

import com.example.purchaseorder.dto.PurchaseOrderPatchRequest;
import com.example.purchaseorder.dto.PurchaseOrderRequest;
import com.example.purchaseorder.dto.PurchaseOrderResponse;
import com.example.purchaseorder.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@Validated
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(@Valid @RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.ok(PurchaseOrderResponse.from(purchaseOrderService.create(request)));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<PurchaseOrderResponse>> createBulk(@Valid @RequestBody List<@Valid PurchaseOrderRequest> requests) {
        List<PurchaseOrderResponse> responses = purchaseOrderService.createBulk(requests).stream()
                .map(PurchaseOrderResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.ok(PurchaseOrderResponse.from(purchaseOrderService.update(id, request)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> patch(@PathVariable Long id,
                                                       @Valid @RequestBody PurchaseOrderPatchRequest request) {
        return ResponseEntity.ok(PurchaseOrderResponse.from(purchaseOrderService.patch(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(PurchaseOrderResponse.from(purchaseOrderService.get(id)));
    }

    @GetMapping
    public ResponseEntity<Page<PurchaseOrderResponse>> list(Pageable pageable) {
        Page<PurchaseOrderResponse> response = purchaseOrderService.list(pageable)
                .map(PurchaseOrderResponse::from);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        purchaseOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> deletePermanent(@PathVariable Long id) {
        purchaseOrderService.deletePermanent(id);
        return ResponseEntity.noContent().build();
    }
}
