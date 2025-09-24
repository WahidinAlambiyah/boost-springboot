package com.example.purchaseorder.service;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.dto.ItemPatchRequest;
import com.example.purchaseorder.dto.ItemRequest;
import com.example.purchaseorder.exception.ResourceNotFoundException;
import com.example.purchaseorder.repository.ItemRepository;
import com.example.purchaseorder.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
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
    }

    @Test
    void createShouldSaveItem() {
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item saved = itemService.create(request);

        assertThat(saved.getName()).isEqualTo("Keyboard");
        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(captor.getValue().getCreatedDatetime()).isNotNull();
        assertThat(captor.getValue().getUpdatedBy()).isNull();
        assertThat(captor.getValue().getUpdatedDatetime()).isNull();
    }

    @Test
    void updateShouldModifyExistingItem() {
        Item existing = Item.builder()
                .id(1L)
                .name("Old")
                .createdBy("original")
                .createdDatetime(OffsetDateTime.now())
                .build();
        when(itemRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(existing)).thenReturn(existing);

        Item updated = itemService.update(1L, request);

        assertThat(updated.getName()).isEqualTo("Keyboard");
        assertThat(existing.getCreatedBy()).isEqualTo("original");
        assertThat(existing.getUpdatedBy()).isEqualTo("SYSTEM");
        assertThat(existing.getUpdatedDatetime()).isNotNull();
    }

    @Test
    void updateShouldThrowWhenNotFound() {
        when(itemRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void patchShouldUpdateProvidedFields() {
        Item existing = Item.builder()
                .id(1L)
                .name("Old")
                .description("Legacy")
                .price(BigDecimal.ONE)
                .build();
        when(itemRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(existing)).thenReturn(existing);

        ItemPatchRequest patchRequest = new ItemPatchRequest();
        patchRequest.setDescription("Updated");
        patchRequest.setPrice(BigDecimal.TEN);

        Item patched = itemService.patch(1L, patchRequest);

        assertThat(patched.getName()).isEqualTo("Old");
        assertThat(patched.getDescription()).isEqualTo("Updated");
        assertThat(patched.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(existing.getUpdatedBy()).isEqualTo("SYSTEM");
        assertThat(existing.getUpdatedDatetime()).isNotNull();
        verify(itemRepository).save(existing);
    }

    @Test
    void deleteShouldSoftDeleteItem() {
        Item existing = Item.builder().id(1L).build();
        when(itemRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(existing)).thenReturn(existing);

        itemService.delete(1L);

        assertThat(existing.isDeleted()).isTrue();
        verify(itemRepository).save(existing);
    }

    @Test
    void deletePermanentShouldRemoveItem() {
        Item existing = Item.builder().id(1L).build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existing));

        itemService.deletePermanent(1L);

        verify(itemRepository).delete(existing);
    }

    @Test
    void getShouldReturnItem() {
        Item existing = Item.builder().id(1L).build();
        when(itemRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));

        assertThat(itemService.get(1L)).isEqualTo(existing);
    }

    @Test
    void listShouldReturnAllItems() {
        List<Item> items = List.of(Item.builder().id(1L).build());
        Page<Item> page = new PageImpl<>(items);
        PageRequest pageable = PageRequest.of(0, 10);
        when(itemRepository.findAllByDeletedFalse(pageable)).thenReturn(page);

        Page<Item> result = itemService.list(pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(itemRepository).findAllByDeletedFalse(pageable);
    }

    @Test
    void createBulkShouldReturnEmptyWhenRequestsNullOrEmpty() {
        assertThat(itemService.createBulk(null)).isEmpty();
        assertThat(itemService.createBulk(Collections.emptyList())).isEmpty();

        verifyNoInteractions(itemRepository);
    }

    @Test
    void createBulkShouldCreateMultipleItems() {
        ItemRequest second = new ItemRequest();
        second.setName("Mouse");
        second.setDescription("Wireless");
        second.setPrice(BigDecimal.ONE);

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Item> results = itemService.createBulk(List.of(request, second));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("Keyboard");
        assertThat(results.get(1).getName()).isEqualTo("Mouse");
        assertThat(results.get(0).getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(results.get(1).getCreatedBy()).isEqualTo("SYSTEM");

        verify(itemRepository, times(2)).save(any(Item.class));
    }
}
