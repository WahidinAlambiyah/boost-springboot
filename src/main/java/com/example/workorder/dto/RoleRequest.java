package com.example.workorder.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class RoleRequest {

    @NotBlank
    private String name;

    private String description;

    private List<Long> accessIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getAccessIds() {
        return accessIds;
    }

    public void setAccessIds(List<Long> accessIds) {
        this.accessIds = accessIds;
    }
}
