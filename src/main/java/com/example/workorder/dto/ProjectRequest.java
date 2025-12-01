package com.example.workorder.dto;

import com.example.workorder.domain.ProjectStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ProjectRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String manager;

    @NotBlank
    private String room;

    @NotNull
    private ProjectStatus status;

    @NotNull
    private Long divisionId;

    @FutureOrPresent
    private LocalDateTime startTime;

    private LocalDateTime endTime;

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

    public Long getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Long divisionId) {
        this.divisionId = divisionId;
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
