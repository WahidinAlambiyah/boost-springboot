package com.example.purchaseorder.dto;

import com.example.purchaseorder.domain.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String createdBy;
    private OffsetDateTime createdDatetime;
    private String updatedBy;
    private OffsetDateTime updatedDatetime;

    public static ItemResponse from(Item item) {
        if (item == null) {
            return null;
        }
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .createdBy(item.getCreatedBy())
                .createdDatetime(item.getCreatedDatetime())
                .updatedBy(item.getUpdatedBy())
                .updatedDatetime(item.getUpdatedDatetime())
                .build();
    }
}
