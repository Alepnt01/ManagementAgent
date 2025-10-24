package com.managementagent.server.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Team aggregate containing its members for collaboration features.
 */
public class Team {
    private Long id;
    private String name;
    private String description;
    private final List<Employee> members = new ArrayList<>();

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

    public List<Employee> getMembers() {
        return members;
    }
}
