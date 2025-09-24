package com.example.purchaseorder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemPatchRequest {

    private static final String NOT_BLANK_REGEX = "^(?!\\s*$).+";

    @Pattern(regexp = NOT_BLANK_REGEX, message = "Name must not be blank.")
    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero.")
    private BigDecimal price;
}
