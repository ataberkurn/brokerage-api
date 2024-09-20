package com.brokerage.api.enumeration;

public enum Role {
    CUSTOMER, EMPLOYEE;

    public String getAuthority() {
        return name();
    }
}
