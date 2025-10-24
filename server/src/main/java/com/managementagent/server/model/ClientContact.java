package com.managementagent.server.model;

/**
 * Lightweight client representation for communications.
 */
public class ClientContact extends Person {
    private String companyName;
    private String vatNumber;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }
}
