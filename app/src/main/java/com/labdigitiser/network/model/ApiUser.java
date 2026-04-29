package com.labdigitiser.network.model;

public class ApiUser {

    private int id;
    private String name;
    private String email;
    private String role;
    private int is_active;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return is_active == 1;
    }
}
