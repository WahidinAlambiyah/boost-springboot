package com.example.purchaseorder.dto;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.domain.PurchaseOrderDetail;
import com.example.purchaseorder.domain.PurchaseOrderHeader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseOrderResponseTest {

    @Test
    void fromShouldMapHeaderAndDetails() {
        OffsetDateTime now = OffsetDateTime.now();
        Item item = Item.builder()
                .id(1L)
                .name("Keyboard")
                .build();
        PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                .id(5L)
                .item(item)
                .itemQty(2)
                .itemCost(BigDecimal.valueOf(200))
                .itemPrice(BigDecimal.valueOf(240))
                .createdBy("creator")
                .createdDatetime(now.minusHours(1))
                .updatedBy("updater")
                .updatedDatetime(now)
                .build();
        PurchaseOrderHeader header = PurchaseOrderHeader.builder()
                .id(9L)
                .datetime(now)
                .description("Office setup")
                .totalCost(BigDecimal.valueOf(200))
                .totalPrice(BigDecimal.valueOf(240))
                .createdBy("creator")
                .createdDatetime(now.minusDays(1))
                .updatedBy("updater")
                .updatedDatetime(now)
                .details(List.of(detail))
                .build();

        PurchaseOrderResponse response = PurchaseOrderResponse.from(header);

        assertThat(response.getId()).isEqualTo(9L);
        assertThat(response.getDatetime()).isEqualTo(now);
        assertThat(response.getDescription()).isEqualTo("Office setup");
        assertThat(response.getTotalCost()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(response.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(240));
        assertThat(response.getCreatedBy()).isEqualTo("creator");
        assertThat(response.getCreatedDatetime()).isEqualTo(now.minusDays(1));
        assertThat(response.getUpdatedBy()).isEqualTo("updater");
        assertThat(response.getUpdatedDatetime()).isEqualTo(now);
        assertThat(response.getDetails()).hasSize(1);
        PurchaseOrderDetailResponse detailResponse = response.getDetails().get(0);
        assertThat(detailResponse.getItemId()).isEqualTo(1L);
        assertThat(detailResponse.getItemName()).isEqualTo("Keyboard");
    }

    @Test
    void fromShouldReturnNullWhenHeaderIsNull() {
        assertThat(PurchaseOrderResponse.from(null)).isNull();
    }

    @Test
    void fromShouldHandleNullDetails() {
        OffsetDateTime now = OffsetDateTime.now();
        PurchaseOrderHeader header = PurchaseOrderHeader.builder()
                .id(2L)
                .datetime(now)
                .totalCost(BigDecimal.TEN)
                .totalPrice(BigDecimal.TEN)
                .details(null)
                .build();

        PurchaseOrderResponse response = PurchaseOrderResponse.from(header);

        assertThat(response.getDetails()).isNull();
    }
}
