package com.labdigitiser.network.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

public class ApiSessionData {

    private ApiUser user;
    private JsonElement permissions;
    private List<ApiPlant> plants;

    public ApiUser getUser() {
        return user;
    }

    public JsonElement getPermissions() {
        return permissions;
    }

    public JsonObject getPermissionsObject() {
        return permissions != null && permissions.isJsonObject() ? permissions.getAsJsonObject() : null;
    }

    public List<ApiPlant> getPlants() {
        return plants;
    }
}
