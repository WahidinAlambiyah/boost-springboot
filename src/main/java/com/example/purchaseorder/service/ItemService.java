package com.example.purchaseorder.service;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.dto.ItemRequest;

import java.util.List;

public interface ItemService {
    Item create(ItemRequest request);

    Item update(Long id, ItemRequest request);

    void delete(Long id);

    Item get(Long id);

    List<Item> list();
}
