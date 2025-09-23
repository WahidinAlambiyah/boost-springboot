package com.example.purchaseorder.service.impl;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.dto.ItemRequest;
import com.example.purchaseorder.exception.ResourceNotFoundException;
import com.example.purchaseorder.repository.ItemRepository;
import com.example.purchaseorder.service.ItemService;
import com.example.purchaseorder.service.util.AuditUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public Item create(ItemRequest request) {
        return createItem(request);
    }

    @Override
    public List<Item> createBulk(List<ItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }
        return requests.stream()
                .map(this::createItem)
                .collect(Collectors.toList());
    }

    @Override
    public Item update(Long id, ItemRequest request) {
        Item item = get(id);
        applyRequest(item, request);
        applyUpdateAudit(item);
        return itemRepository.save(item);
    }

    @Override
    public void delete(Long id) {
        Item item = itemRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
        item.setDeleted(true);
        itemRepository.save(item);
    }

    @Override
    public void deletePermanent(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
        itemRepository.delete(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Item get(Long id) {
        return itemRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Item> list(Pageable pageable) {
        return itemRepository.findAllByDeletedFalse(pageable);
    }

    private void applyRequest(Item item, ItemRequest request) {
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
    }

    private Item createItem(ItemRequest request) {
        Item item = new Item();
        applyRequest(item, request);
        applyCreationAudit(item);
        item.setDeleted(false);
        return itemRepository.save(item);
    }

    private void applyCreationAudit(Item item) {
        String auditor = AuditUtils.resolveCurrentAuditor();
        OffsetDateTime now = AuditUtils.currentDateTime();
        item.setCreatedBy(auditor);
        item.setCreatedDatetime(now);
        item.setUpdatedBy(null);
        item.setUpdatedDatetime(null);
    }

    private void applyUpdateAudit(Item item) {
        String auditor = AuditUtils.resolveCurrentAuditor();
        OffsetDateTime now = AuditUtils.currentDateTime();
        item.setUpdatedBy(auditor);
        item.setUpdatedDatetime(now);
    }
}
