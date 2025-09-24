package com.example.purchaseorder.dto;

import com.example.purchaseorder.domain.Item;
import com.example.purchaseorder.domain.PurchaseOrderDetail;
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
public class PurchaseOrderDetailResponse {

    private Long id;
    private Long itemId;
    private String itemName;
    private Integer itemQty;
    private BigDecimal itemCost;
    private BigDecimal itemPrice;
    private String createdBy;
    private OffsetDateTime createdDatetime;
    private String updatedBy;
    private OffsetDateTime updatedDatetime;

    public static PurchaseOrderDetailResponse from(PurchaseOrderDetail detail) {
        if (detail == null) {
            return null;
        }
        Item item = detail.getItem();
        return PurchaseOrderDetailResponse.builder()
                .id(detail.getId())
                .itemId(item != null ? item.getId() : null)
                .itemName(item != null ? item.getName() : null)
                .itemQty(detail.getItemQty())
                .itemCost(detail.getItemCost())
                .itemPrice(detail.getItemPrice())
                .createdBy(detail.getCreatedBy())
                .createdDatetime(detail.getCreatedDatetime())
                .updatedBy(detail.getUpdatedBy())
                .updatedDatetime(detail.getUpdatedDatetime())
                .build();
    }
}
