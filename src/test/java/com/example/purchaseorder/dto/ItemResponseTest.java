package com.example.purchaseorder.dto;

import com.example.purchaseorder.domain.Item;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ItemResponseTest {

    @Test
    void fromShouldMapItemFields() {
        OffsetDateTime created = OffsetDateTime.now().minusHours(2);
        OffsetDateTime updated = OffsetDateTime.now();
        Item item = Item.builder()
                .id(99L)
                .name("Keyboard")
                .description("Mechanical")
                .price(BigDecimal.TEN)
                .createdBy("creator")
                .createdDatetime(created)
                .updatedBy("updater")
                .updatedDatetime(updated)
                .build();

        ItemResponse response = ItemResponse.from(item);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getName()).isEqualTo("Keyboard");
        assertThat(response.getDescription()).isEqualTo("Mechanical");
        assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(response.getCreatedBy()).isEqualTo("creator");
        assertThat(response.getCreatedDatetime()).isEqualTo(created);
        assertThat(response.getUpdatedBy()).isEqualTo("updater");
        assertThat(response.getUpdatedDatetime()).isEqualTo(updated);
    }

    @Test
    void fromShouldReturnNullWhenItemIsNull() {
        assertThat(ItemResponse.from(null)).isNull();
    }
}
