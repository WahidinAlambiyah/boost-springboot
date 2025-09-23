package com.example.purchaseorder.controller;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.dto.ItemRequest;
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

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<Item> create(@RequestBody ItemRequest request) {
        return ResponseEntity.ok(itemService.create(request));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Item>> createBulk(@RequestBody List<ItemRequest> requests) {
        return ResponseEntity.ok(itemService.createBulk(requests));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> update(@PathVariable Long id, @RequestBody ItemRequest request) {
        return ResponseEntity.ok(itemService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> get(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.get(id));
    }

    @GetMapping
    public ResponseEntity<Page<Item>> list(Pageable pageable) {
        return ResponseEntity.ok(itemService.list(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
