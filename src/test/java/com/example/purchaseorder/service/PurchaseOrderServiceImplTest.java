package com.example.purchaseorder.service;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.domain.PurchaseOrderDetail;
import com.example.purchaseorder.domain.PurchaseOrderHeader;
import com.example.purchaseorder.dto.PurchaseOrderDetailRequest;
import com.example.purchaseorder.dto.PurchaseOrderPatchRequest;
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

        request = new PurchaseOrderRequest();
        request.setDatetime(OffsetDateTime.now());
        request.setDescription("Test PO");
        request.setDetails(List.of(detailRequest));
    }

    @Test
    void createShouldPersistHeaderAndDetails() {
        Item item = Item.builder().id(10L).build();
        when(itemRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(item));
        when(headerRepository.save(any(PurchaseOrderHeader.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrderHeader saved = purchaseOrderService.create(request);

        assertThat(saved.getDetails()).hasSize(1);
        PurchaseOrderDetail detail = saved.getDetails().get(0);
        assertThat(detail.getItem()).isEqualTo(item);
        assertThat(saved.getTotalCost()).isEqualTo(new BigDecimal("10.00"));
        assertThat(saved.getTotalPrice()).isEqualTo(new BigDecimal("15.00"));
        assertThat(saved.getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(saved.getCreatedDatetime()).isNotNull();
        assertThat(saved.getUpdatedBy()).isNull();
        assertThat(saved.getUpdatedDatetime()).isNull();
        assertThat(detail.getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(detail.getCreatedDatetime()).isNotNull();
        assertThat(detail.getUpdatedBy()).isNull();
        assertThat(detail.getUpdatedDatetime()).isNull();
    }

    @Test
    void updateShouldReplaceExistingDetails() {
        PurchaseOrderHeader existing = new PurchaseOrderHeader();
        existing.setId(1L);
        existing.setCreatedBy("original");
        existing.setCreatedDatetime(OffsetDateTime.now().minusDays(1));
        existing.setDetails(new java.util.ArrayList<>(List.of(PurchaseOrderDetail.builder().build())));
        when(headerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(Item.builder().id(10L).build()));
        when(headerRepository.save(existing)).thenReturn(existing);

        PurchaseOrderHeader updated = purchaseOrderService.update(1L, request);

        assertThat(updated.getDetails()).hasSize(1);
        assertThat(updated.getDetails().get(0).getItemQty()).isEqualTo(2);
        assertThat(existing.getCreatedBy()).isEqualTo("original");
        assertThat(existing.getUpdatedBy()).isEqualTo("SYSTEM");
        assertThat(existing.getUpdatedDatetime()).isNotNull();
        PurchaseOrderDetail updatedDetail = updated.getDetails().get(0);
        assertThat(updatedDetail.getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(updatedDetail.getCreatedDatetime()).isNotNull();
        assertThat(updatedDetail.getUpdatedBy()).isEqualTo("SYSTEM");
        assertThat(updatedDetail.getUpdatedDatetime()).isNotNull();
    }

    @Test
    void updateShouldThrowWhenNotFound() {
        when(headerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> purchaseOrderService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void patchShouldUpdateSelectedFields() {
        PurchaseOrderHeader existing = new PurchaseOrderHeader();
        existing.setId(1L);
        existing.setDescription("Original");
        PurchaseOrderDetail existingDetail = PurchaseOrderDetail.builder().itemQty(1).build();
        existing.setDetails(new java.util.ArrayList<>(List.of(existingDetail)));

        Item item = Item.builder().id(10L).build();
        when(headerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(item));
        when(headerRepository.save(existing)).thenReturn(existing);

        PurchaseOrderDetailRequest detailRequest = new PurchaseOrderDetailRequest();
        detailRequest.setItemId(10L);
        detailRequest.setItemQty(3);
        detailRequest.setItemCost(new BigDecimal("2.00"));
        detailRequest.setItemPrice(new BigDecimal("4.00"));

        PurchaseOrderPatchRequest patchRequest = new PurchaseOrderPatchRequest();
        patchRequest.setDescription("Patched");
        patchRequest.setDetails(List.of(detailRequest));
        patchRequest.setTotalPrice(new BigDecimal("50.00"));

        PurchaseOrderHeader patched = purchaseOrderService.patch(1L, patchRequest);

        assertThat(patched.getDescription()).isEqualTo("Patched");
        assertThat(patched.getDetails()).hasSize(1);
        PurchaseOrderDetail patchedDetail = patched.getDetails().get(0);
        assertThat(patchedDetail.getItem()).isEqualTo(item);
        assertThat(patchedDetail.getItemQty()).isEqualTo(3);
        assertThat(patched.getTotalCost()).isEqualTo(new BigDecimal("6.00"));
        assertThat(patched.getTotalPrice()).isEqualTo(new BigDecimal("50.00"));
        assertThat(existing.getUpdatedBy()).isEqualTo("SYSTEM");
        assertThat(existing.getUpdatedDatetime()).isNotNull();
    }

    @Test
    void patchShouldRetainTotalsWhenDetailsOmittedAndTotalsNotProvided() {
        PurchaseOrderHeader existing = new PurchaseOrderHeader();
        existing.setId(1L);
        existing.setDescription("Original");
        existing.setTotalCost(new BigDecimal("25.00"));
        existing.setTotalPrice(new BigDecimal("40.00"));
        existing.setDetails(new java.util.ArrayList<>(List.of(PurchaseOrderDetail.builder().itemQty(1).build())));

        when(headerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(headerRepository.save(existing)).thenReturn(existing);

        PurchaseOrderPatchRequest patchRequest = new PurchaseOrderPatchRequest();
        patchRequest.setDescription("Retained totals");

        PurchaseOrderHeader patched = purchaseOrderService.patch(1L, patchRequest);

        assertThat(patched.getDescription()).isEqualTo("Retained totals");
        assertThat(patched.getTotalCost()).isEqualTo(new BigDecimal("25.00"));
        assertThat(patched.getTotalPrice()).isEqualTo(new BigDecimal("40.00"));
        assertThat(existing.getUpdatedBy()).isEqualTo("SYSTEM");
        assertThat(existing.getUpdatedDatetime()).isNotNull();
        verify(headerRepository).save(existing);
    }

    @Test
    void deleteShouldSoftDeleteHeader() {
        PurchaseOrderHeader existing = new PurchaseOrderHeader();
        existing.setId(1L);
        when(headerRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(headerRepository.save(existing)).thenReturn(existing);

        purchaseOrderService.delete(1L);

        assertThat(existing.isDeleted()).isTrue();
        verify(headerRepository).save(existing);
    }

    @Test
    void deletePermanentShouldRemoveHeader() {
        PurchaseOrderHeader existing = new PurchaseOrderHeader();
        existing.setId(1L);
        when(headerRepository.findById(1L)).thenReturn(Optional.of(existing));

        purchaseOrderService.deletePermanent(1L);

        verify(headerRepository).delete(existing);
    }

    @Test
    void listShouldReturnAllHeaders() {
        List<PurchaseOrderHeader> headers = List.of(new PurchaseOrderHeader());
        Page<PurchaseOrderHeader> page = new PageImpl<>(headers);
        PageRequest pageable = PageRequest.of(0, 10);
        when(headerRepository.findAllByDeletedFalse(pageable)).thenReturn(page);

        Page<PurchaseOrderHeader> result = purchaseOrderService.list(pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(headerRepository).findAllByDeletedFalse(pageable);
    }

    @Test
    void createBulkShouldReturnEmptyWhenRequestsNullOrEmpty() {
        assertThat(purchaseOrderService.createBulk(null)).isEmpty();
        assertThat(purchaseOrderService.createBulk(Collections.emptyList())).isEmpty();

        verifyNoInteractions(headerRepository, itemRepository);
    }

    @Test
    void createBulkShouldPersistMultiplePurchaseOrders() {
        Item itemOne = Item.builder().id(10L).build();
        Item itemTwo = Item.builder().id(11L).build();
        when(itemRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(itemOne));
        when(itemRepository.findByIdAndDeletedFalse(11L)).thenReturn(Optional.of(itemTwo));
        when(headerRepository.save(any(PurchaseOrderHeader.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrderDetailRequest secondDetail = new PurchaseOrderDetailRequest();
        secondDetail.setItemId(11L);
        secondDetail.setItemQty(1);
        secondDetail.setItemCost(new BigDecimal("3.00"));
        secondDetail.setItemPrice(new BigDecimal("5.00"));

        PurchaseOrderRequest second = new PurchaseOrderRequest();
        second.setDatetime(OffsetDateTime.now().plusDays(1));
        second.setDescription("Second PO");
        second.setDetails(List.of(secondDetail));

        List<PurchaseOrderHeader> results = purchaseOrderService.createBulk(List.of(request, second));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getDetails()).hasSize(1);
        assertThat(results.get(1).getDetails()).hasSize(1);
        assertThat(results.get(0).getTotalCost()).isEqualTo(new BigDecimal("10.00"));
        assertThat(results.get(0).getTotalPrice()).isEqualTo(new BigDecimal("15.00"));
        assertThat(results.get(1).getTotalCost()).isEqualTo(new BigDecimal("3.00"));
        assertThat(results.get(1).getTotalPrice()).isEqualTo(new BigDecimal("5.00"));
        assertThat(results.get(1).getDetails().get(0).getItem()).isEqualTo(itemTwo);
        assertThat(results.get(0).isDeleted()).isFalse();

        verify(headerRepository, times(2)).save(any(PurchaseOrderHeader.class));
        verify(itemRepository, times(2)).findByIdAndDeletedFalse(anyLong());
    }
}
