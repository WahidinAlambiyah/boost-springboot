package com.example.purchaseorder.service;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.dto.ItemRequest;
import com.example.purchaseorder.exception.ResourceNotFoundException;
import com.example.purchaseorder.repository.ItemRepository;
import com.example.purchaseorder.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private ItemRequest request;

    @BeforeEach
    void setUp() {
        request = new ItemRequest();
        request.setName("Keyboard");
        request.setDescription("Mechanical");
        request.setPrice(BigDecimal.TEN);
        request.setCreatedBy("tester");
        request.setCreatedDatetime(OffsetDateTime.now());
        request.setUpdatedBy("tester");
        request.setUpdatedDatetime(OffsetDateTime.now());
    }

    @Test
    void createShouldSaveItem() {
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item saved = itemService.create(request);

        assertThat(saved.getName()).isEqualTo("Keyboard");
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateShouldModifyExistingItem() {
        Item existing = Item.builder().id(1L).name("Old").build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(existing)).thenReturn(existing);

        Item updated = itemService.update(1L, request);

        assertThat(updated.getName()).isEqualTo("Keyboard");
    }

    @Test
    void updateShouldThrowWhenNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteShouldRemoveItem() {
        Item existing = Item.builder().id(1L).build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existing));

        itemService.delete(1L);

        verify(itemRepository).delete(existing);
    }

    @Test
    void getShouldReturnItem() {
        Item existing = Item.builder().id(1L).build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThat(itemService.get(1L)).isEqualTo(existing);
    }

    @Test
    void listShouldReturnAllItems() {
        List<Item> items = List.of(Item.builder().id(1L).build());
        when(itemRepository.findAll()).thenReturn(items);

        assertThat(itemService.list()).hasSize(1);
    }
}
