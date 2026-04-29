package com.labdigitiser.network;

import android.content.Context;

import com.labdigitiser.network.model.ApiDashboardData;
import com.labdigitiser.network.model.ApiLocation;
import com.labdigitiser.network.model.ApiParameter;
import com.labdigitiser.network.model.ApiReadingDetail;
import com.labdigitiser.network.model.ApiLoginRequest;
import com.labdigitiser.network.model.ApiLoginData;
import com.labdigitiser.network.model.ApiPlant;
import com.labdigitiser.network.model.ApiResponse;
import com.labdigitiser.network.model.ApiSessionData;
import com.labdigitiser.network.model.ApiWriteResponse;
import com.labdigitiser.network.model.CreateReadingRequest;

import java.util.List;

import retrofit2.Call;

public class WebsiteRepository {

    private final LabDigitiserApi api;
    private final SessionStore sessionStore;

    public WebsiteRepository(Context context) {
        api = ApiClient.getService(context);
        sessionStore = new SessionStore(context);
    }

    public Call<ApiResponse<ApiLoginData>> login(String email, String password) {
        return api.login(new ApiLoginRequest(email, password));
    }

    public void saveAuthToken(String token) {
        sessionStore.saveAuthToken(token);
    }

    public boolean hasActiveSession() {
        return sessionStore.hasAuthToken();
    }

    public Call<ApiResponse<ApiSessionData>> getCurrentUser() {
        return api.getCurrentUser();
    }

    public Call<ApiResponse<List<ApiPlant>>> getPlants() {
        return api.getPlants();
    }

    public Call<ApiResponse<ApiDashboardData>> getDashboard() {
        return api.getDashboard(sessionStore.getSelectedPlantId());
    }

    public Call<ApiResponse<List<ApiLocation>>> getPlantLocations() {
        return api.getPlantLocations(sessionStore.getSelectedPlantId());
    }

    public Call<ApiResponse<List<ApiParameter>>> getPlantParameters() {
        return api.getPlantParameters(sessionStore.getSelectedPlantId());
    }

    public void selectPlant(String plantId, String plantName) {
        sessionStore.saveSelectedPlant(plantId, plantName);
    }

    public Call<ApiWriteResponse> createReading(CreateReadingRequest request) {
        return api.createReading(request);
    }

    public Call<ApiResponse<ApiReadingDetail>> getReading(String readingId) {
        return api.getReading(readingId);
    }

    public void clearSession() {
        sessionStore.clear();
    }

    public String getSelectedPlantId() {
        return sessionStore.getSelectedPlantId();
    }

    public String getSelectedPlantName() {
        return sessionStore.getSelectedPlantName();
    }

    public String getBaseUrl() {
        return com.labdigitiser.BuildConfig.LABDIGITISER_BASE_URL;
    }
}
