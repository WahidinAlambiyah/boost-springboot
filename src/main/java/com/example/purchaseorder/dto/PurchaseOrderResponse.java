package com.example.purchaseorder.dto;

import com.example.purchaseorder.domain.PurchaseOrderDetail;
import com.example.purchaseorder.domain.PurchaseOrderHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {

    private Long id;
    private OffsetDateTime datetime;
    private String description;
    private BigDecimal totalPrice;
    private BigDecimal totalCost;
    private String createdBy;
    private OffsetDateTime createdDatetime;
    private String updatedBy;
    private OffsetDateTime updatedDatetime;
    private List<PurchaseOrderDetailResponse> details;

    public static PurchaseOrderResponse from(PurchaseOrderHeader header) {
        if (header == null) {
            return null;
        }
        List<PurchaseOrderDetailResponse> detailResponses = null;
        List<PurchaseOrderDetail> details = header.getDetails();
        if (details != null) {
            detailResponses = details.stream()
                    .map(PurchaseOrderDetailResponse::from)
                    .collect(Collectors.toList());
        }
        return PurchaseOrderResponse.builder()
                .id(header.getId())
                .datetime(header.getDatetime())
                .description(header.getDescription())
                .totalPrice(header.getTotalPrice())
                .totalCost(header.getTotalCost())
                .createdBy(header.getCreatedBy())
                .createdDatetime(header.getCreatedDatetime())
                .updatedBy(header.getUpdatedBy())
                .updatedDatetime(header.getUpdatedDatetime())
                .details(detailResponses)
                .build();
    }
}
