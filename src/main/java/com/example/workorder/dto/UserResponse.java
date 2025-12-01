package com.example.workorder.dto;

import java.util.List;

public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Long divisionId;
    private String divisionName;
    private List<RoleResponse> roles;

    public UserResponse() {
    }

    public UserResponse(Long id, String username, String fullName, String email, Long divisionId, String divisionName, List<RoleResponse> roles) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.divisionId = divisionId;
        this.divisionName = divisionName;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Long divisionId) {
        this.divisionId = divisionId;
    }

    public String getDivisionName() {
        return divisionName;
    }

    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public List<RoleResponse> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleResponse> roles) {
        this.roles = roles;
    }
}
