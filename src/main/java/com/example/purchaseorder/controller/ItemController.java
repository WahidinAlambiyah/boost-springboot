package com.example.purchaseorder.controller;

import com.example.purchaseorder.dto.ItemRequest;
import com.example.purchaseorder.dto.ItemResponse;
import com.example.purchaseorder.service.ItemService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponse> create(@RequestBody ItemRequest request) {
        return ResponseEntity.ok(ItemResponse.from(itemService.create(request)));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ItemResponse>> createBulk(@RequestBody List<ItemRequest> requests) {
        List<ItemResponse> responses = itemService.createBulk(requests).stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> update(@PathVariable Long id, @RequestBody ItemRequest request) {
        return ResponseEntity.ok(ItemResponse.from(itemService.update(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(ItemResponse.from(itemService.get(id)));
    }

    @GetMapping
    public ResponseEntity<Page<ItemResponse>> list(Pageable pageable) {
        Page<ItemResponse> response = itemService.list(pageable)
                .map(ItemResponse::from);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
