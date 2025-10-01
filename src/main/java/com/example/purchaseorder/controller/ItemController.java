package com.example.purchaseorder.controller;

import com.example.purchaseorder.dto.ItemPatchRequest;
import com.example.purchaseorder.dto.ItemRequest;
import com.example.purchaseorder.dto.ItemResponse;
import com.example.purchaseorder.security.SecurityRoles;
import com.example.purchaseorder.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @PreAuthorize(SecurityRoles.HAS_ROLE_ADMIN)
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(ItemResponse.from(itemService.create(request)));
    }

    @PostMapping("/bulk")
    @PreAuthorize(SecurityRoles.HAS_ROLE_ADMIN)
    public ResponseEntity<List<ItemResponse>> createBulk(@Valid @RequestBody List<@Valid ItemRequest> requests) {
        List<ItemResponse> responses = itemService.createBulk(requests).stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityRoles.HAS_ROLE_ADMIN)
    public ResponseEntity<ItemResponse> update(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(ItemResponse.from(itemService.update(id, request)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize(SecurityRoles.HAS_ROLE_ADMIN)
    public ResponseEntity<ItemResponse> patch(@PathVariable Long id, @Valid @RequestBody ItemPatchRequest request) {
        return ResponseEntity.ok(ItemResponse.from(itemService.patch(id, request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityRoles.HAS_ANY_ROLE_ADMIN_OR_USER)
    public ResponseEntity<ItemResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(ItemResponse.from(itemService.get(id)));
    }

    @GetMapping
    @PreAuthorize(SecurityRoles.HAS_ANY_ROLE_ADMIN_OR_USER)
    public ResponseEntity<Page<ItemResponse>> list(Pageable pageable) {
        Page<ItemResponse> response = itemService.list(pageable)
                .map(ItemResponse::from);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityRoles.HAS_ROLE_ADMIN)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize(SecurityRoles.HAS_ROLE_ADMIN)
    public ResponseEntity<Void> deletePermanent(@PathVariable Long id) {
        itemService.deletePermanent(id);
        return ResponseEntity.noContent().build();
    }
}
