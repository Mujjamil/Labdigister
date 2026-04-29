package com.labdigitiser.network.model;

import com.google.gson.JsonObject;

import java.util.List;

public class ApiLoginData {

    private String token;
    private String token_type;
    private int expires_in;
    private ApiUser user;
    private JsonObject permissions;
    private List<ApiPlant> plants;

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return token_type;
    }

    public int getExpiresIn() {
        return expires_in;
    }

    public ApiUser getUser() {
        return user;
    }

    public JsonObject getPermissions() {
        return permissions;
    }

    public List<ApiPlant> getPlants() {
        return plants;
    }
}
