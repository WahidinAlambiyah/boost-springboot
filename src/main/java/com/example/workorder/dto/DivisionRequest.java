package com.example.workorder.dto;

import jakarta.validation.constraints.NotBlank;

public class DivisionRequest {

    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
