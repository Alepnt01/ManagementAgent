package com.managementagent.client.model;

import java.util.ArrayList;
import java.util.List;

public class TeamPayload {
    private Long id;
    private String name;
    private String description;
    private List<EmployeePayload> members = new ArrayList<>();

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

    public List<EmployeePayload> getMembers() {
        return members;
    }

    public void setMembers(List<EmployeePayload> members) {
        this.members = members;
    }
}
