package com.example.purchaseorder.service;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.domain.PurchaseOrderDetail;
import com.example.purchaseorder.domain.PurchaseOrderHeader;
import com.example.purchaseorder.dto.PurchaseOrderDetailRequest;
import com.example.purchaseorder.dto.PurchaseOrderRequest;
import com.example.purchaseorder.exception.ResourceNotFoundException;
import com.example.purchaseorder.repository.ItemRepository;
import com.example.purchaseorder.repository.PurchaseOrderHeaderRepository;
import com.example.purchaseorder.service.impl.PurchaseOrderServiceImpl;
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
class PurchaseOrderServiceImplTest {

    @Mock
    private PurchaseOrderHeaderRepository headerRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    private PurchaseOrderRequest request;

    @BeforeEach
    void setUp() {
        PurchaseOrderDetailRequest detailRequest = new PurchaseOrderDetailRequest();
        detailRequest.setItemId(10L);
        detailRequest.setItemQty(2);
        detailRequest.setItemCost(new BigDecimal("5.00"));
        detailRequest.setItemPrice(new BigDecimal("7.50"));
        detailRequest.setCreatedBy("tester");
        detailRequest.setCreatedDatetime(OffsetDateTime.now());
        detailRequest.setUpdatedBy("tester");
        detailRequest.setUpdatedDatetime(OffsetDateTime.now());

        request = new PurchaseOrderRequest();
        request.setDatetime(OffsetDateTime.now());
        request.setDescription("Test PO");
        request.setDetails(List.of(detailRequest));
        request.setCreatedBy("tester");
        request.setCreatedDatetime(OffsetDateTime.now());
        request.setUpdatedBy("tester");
        request.setUpdatedDatetime(OffsetDateTime.now());
    }

    @Test
    void createShouldPersistHeaderAndDetails() {
        Item item = Item.builder().id(10L).build();
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(headerRepository.save(any(PurchaseOrderHeader.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrderHeader saved = purchaseOrderService.create(request);

        assertThat(saved.getDetails()).hasSize(1);
        PurchaseOrderDetail detail = saved.getDetails().get(0);
        assertThat(detail.getItem()).isEqualTo(item);
        assertThat(saved.getTotalCost()).isEqualTo(new BigDecimal("10.00"));
        assertThat(saved.getTotalPrice()).isEqualTo(new BigDecimal("15.00"));
    }

    @Test
    void updateShouldReplaceExistingDetails() {
        PurchaseOrderHeader existing = new PurchaseOrderHeader();
        existing.setId(1L);
        existing.setDetails(new java.util.ArrayList<>(List.of(PurchaseOrderDetail.builder().build())));
        when(headerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(Item.builder().id(10L).build()));
        when(headerRepository.save(existing)).thenReturn(existing);

        PurchaseOrderHeader updated = purchaseOrderService.update(1L, request);

        assertThat(updated.getDetails()).hasSize(1);
        assertThat(updated.getDetails().get(0).getItemQty()).isEqualTo(2);
    }

    @Test
    void updateShouldThrowWhenNotFound() {
        when(headerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> purchaseOrderService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteShouldRemoveHeader() {
        PurchaseOrderHeader existing = new PurchaseOrderHeader();
        existing.setId(1L);
        when(headerRepository.findById(1L)).thenReturn(Optional.of(existing));

        purchaseOrderService.delete(1L);

        verify(headerRepository).delete(existing);
    }

    @Test
    void listShouldReturnAllHeaders() {
        List<PurchaseOrderHeader> headers = List.of(new PurchaseOrderHeader());
        when(headerRepository.findAll()).thenReturn(headers);

        assertThat(purchaseOrderService.list()).hasSize(1);
    }
}
