package com.example.purchaseorder.dto;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.domain.PurchaseOrderDetail;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseOrderDetailResponseTest {

    @Test
    void fromShouldMapDetailFieldsIncludingItem() {
        OffsetDateTime created = OffsetDateTime.now().minusDays(2);
        OffsetDateTime updated = OffsetDateTime.now();
        Item item = Item.builder()
                .id(7L)
                .name("Mouse")
                .build();
        PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                .id(15L)
                .item(item)
                .itemQty(3)
                .itemCost(BigDecimal.valueOf(100))
                .itemPrice(BigDecimal.valueOf(150))
                .createdBy("creator")
                .createdDatetime(created)
                .updatedBy("updater")
                .updatedDatetime(updated)
                .build();

        PurchaseOrderDetailResponse response = PurchaseOrderDetailResponse.from(detail);

        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getItemId()).isEqualTo(7L);
        assertThat(response.getItemName()).isEqualTo("Mouse");
        assertThat(response.getItemQty()).isEqualTo(3);
        assertThat(response.getItemCost()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(response.getItemPrice()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(response.getCreatedBy()).isEqualTo("creator");
        assertThat(response.getCreatedDatetime()).isEqualTo(created);
        assertThat(response.getUpdatedBy()).isEqualTo("updater");
        assertThat(response.getUpdatedDatetime()).isEqualTo(updated);
    }

    @Test
    void fromShouldHandleNullItemAndDetail() {
        PurchaseOrderDetail detailWithoutItem = PurchaseOrderDetail.builder()
                .id(1L)
                .item(null)
                .itemQty(1)
                .itemCost(BigDecimal.ONE)
                .itemPrice(BigDecimal.ONE)
                .build();

        PurchaseOrderDetailResponse response = PurchaseOrderDetailResponse.from(detailWithoutItem);

        assertThat(response.getItemId()).isNull();
        assertThat(response.getItemName()).isNull();

        assertThat(PurchaseOrderDetailResponse.from(null)).isNull();
    }
}
