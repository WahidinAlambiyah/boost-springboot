package com.example.purchaseorder.service.impl;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.dto.ItemRequest;
import com.example.purchaseorder.exception.ResourceNotFoundException;
import com.example.purchaseorder.repository.ItemRepository;
import com.example.purchaseorder.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public Item create(ItemRequest request) {
        Item item = new Item();
        applyRequest(item, request);
        return itemRepository.save(item);
    }

    @Override
    public Item update(Long id, ItemRequest request) {
        Item item = get(id);
        applyRequest(item, request);
        return itemRepository.save(item);
    }

    @Override
    public void delete(Long id) {
        itemRepository.delete(get(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Item get(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Item> list(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    private void applyRequest(Item item, ItemRequest request) {
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCreatedBy(request.getCreatedBy());
        item.setCreatedDatetime(request.getCreatedDatetime());
        item.setUpdatedBy(request.getUpdatedBy());
        item.setUpdatedDatetime(request.getUpdatedDatetime());
    }
}
