package com.labdigitiser.network.model;

import com.google.gson.JsonObject;

import java.util.List;

public class ApiSessionData {

    private ApiUser user;
    private JsonObject permissions;
    private List<ApiPlant> plants;

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
