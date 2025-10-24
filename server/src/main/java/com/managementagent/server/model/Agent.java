package com.managementagent.server.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an agent domain entity.
 */
public class Agent {
    private Long id;
    private String code;
    private String name;
    private String region;
    private String status;
    private LocalDateTime lastUpdate;

    public Agent() {
    }

    public Agent(Long id, String code, String name, String region, String status, LocalDateTime lastUpdate) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.region = region;
        this.status = status;
        this.lastUpdate = lastUpdate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return Objects.equals(id, agent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
