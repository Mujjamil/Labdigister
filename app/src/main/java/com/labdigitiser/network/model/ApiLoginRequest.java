package com.labdigitiser.network.model;

public class ApiLoginRequest {

    private final String email;
    private final String password;

    public ApiLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
