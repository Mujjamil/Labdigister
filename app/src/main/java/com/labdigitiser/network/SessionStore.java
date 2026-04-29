package com.labdigitiser.network;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionStore {

    private static final String PREF_NAME = "labdigitiser_api_session";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_SELECTED_PLANT_ID = "selected_plant_id";
    private static final String KEY_SELECTED_PLANT_NAME = "selected_plant_name";

    private final SharedPreferences preferences;

    public SessionStore(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthToken(String token) {
        preferences.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    public String getAuthToken() {
        return preferences.getString(KEY_AUTH_TOKEN, null);
    }

    public boolean hasAuthToken() {
        String token = getAuthToken();
        return token != null && !token.trim().isEmpty();
    }

    public void saveSelectedPlant(String plantId, String plantName) {
        preferences.edit()
                .putString(KEY_SELECTED_PLANT_ID, plantId)
                .putString(KEY_SELECTED_PLANT_NAME, plantName)
                .apply();
    }

    public String getSelectedPlantId() {
        return preferences.getString(KEY_SELECTED_PLANT_ID, null);
    }

    public String getSelectedPlantName() {
        return preferences.getString(KEY_SELECTED_PLANT_NAME, null);
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
