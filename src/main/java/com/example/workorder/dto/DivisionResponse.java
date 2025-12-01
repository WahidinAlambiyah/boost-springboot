package com.example.workorder.dto;

public class DivisionResponse {
    private Long id;
    private String name;

    public DivisionResponse() {
    }

    public DivisionResponse(Long id, String name) {
        this.id = id;
        this.name = name;
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
}
