package com.example.workorder.dto;

import java.util.List;

public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private List<AccessResponse> accesses;

    public RoleResponse() {
    }

    public RoleResponse(Long id, String name, String description, List<AccessResponse> accesses) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.accesses = accesses;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<AccessResponse> getAccesses() {
        return accesses;
    }

    public void setAccesses(List<AccessResponse> accesses) {
        this.accesses = accesses;
    }
}
