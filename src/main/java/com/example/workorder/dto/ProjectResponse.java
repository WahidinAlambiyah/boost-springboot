package com.example.workorder.dto;

import com.example.workorder.domain.ProjectStatus;

import java.time.LocalDateTime;

public class ProjectResponse {
    private Long id;
    private Long divisionId;
    private String divisionName;
    private String name;
    private String manager;
    private String room;
    private ProjectStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public ProjectResponse() {
    }

    public ProjectResponse(Long id, Long divisionId, String divisionName, String name, String manager, String room,
                           ProjectStatus status, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.divisionId = divisionId;
        this.divisionName = divisionName;
        this.name = name;
        this.manager = manager;
        this.room = room;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
