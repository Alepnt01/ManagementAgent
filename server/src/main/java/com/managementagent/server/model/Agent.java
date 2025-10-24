package com.managementagent.server.model;

import java.time.LocalDateTime;

/**
 * Represents an agent domain entity, extending the base {@link Person} data.
 */
public class Agent extends Person {
    private String code;
    private String region;
    private String status;
    private LocalDateTime lastUpdate;

    public Agent() {
    }

    public Agent(Long id, String name, String email, String phone, String code, String region, String status, LocalDateTime lastUpdate) {
        super(id, name, email, phone);
        this.code = code;
        this.region = region;
        this.status = status;
        this.lastUpdate = lastUpdate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
