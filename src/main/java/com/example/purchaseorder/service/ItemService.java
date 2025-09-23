package com.example.purchaseorder.service;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.dto.ItemRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemService {
    Item create(ItemRequest request);

    Item update(Long id, ItemRequest request);

    void delete(Long id);

    Item get(Long id);

    Page<Item> list(Pageable pageable);
}
