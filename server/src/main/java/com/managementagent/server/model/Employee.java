package com.managementagent.server.model;

/**
 * Employee entity extending person information with job data.
 */
public class Employee extends Person {
    private String jobTitle;

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
}
