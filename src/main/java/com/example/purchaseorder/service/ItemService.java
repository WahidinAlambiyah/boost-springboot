package com.example.purchaseorder.service;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.dto.ItemPatchRequest;
import com.example.purchaseorder.dto.ItemRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemService {
    Item create(ItemRequest request);

    List<Item> createBulk(List<ItemRequest> requests);

    Item update(Long id, ItemRequest request);

    Item patch(Long id, ItemPatchRequest request);

    void delete(Long id);

    void deletePermanent(Long id);

    Item get(Long id);

    Page<Item> list(Pageable pageable);
}
