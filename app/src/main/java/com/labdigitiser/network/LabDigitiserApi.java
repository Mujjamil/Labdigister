package com.labdigitiser.network;

import com.labdigitiser.network.model.ApiDashboardData;
import com.labdigitiser.network.model.ApiLoginData;
import com.labdigitiser.network.model.ApiLoginRequest;
import com.labdigitiser.network.model.ApiParameter;
import com.labdigitiser.network.model.ApiPlant;
import com.labdigitiser.network.model.ApiReadingDetail;
import com.labdigitiser.network.model.ApiResponse;
import com.labdigitiser.network.model.ApiSessionData;
import com.labdigitiser.network.model.ApiWriteResponse;
import com.labdigitiser.network.model.ApiLocation;
import com.labdigitiser.network.model.CreateReadingRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LabDigitiserApi {

    @POST("auth/login")
    Call<ApiResponse<ApiLoginData>> login(@Body ApiLoginRequest request);

    @GET("auth/me")
    Call<ApiResponse<ApiSessionData>> getCurrentUser();

    @GET("plants")
    Call<ApiResponse<List<ApiPlant>>> getPlants();

    @GET("dashboard")
    Call<ApiResponse<ApiDashboardData>> getDashboard(@Query("plant_id") String plantId);

    @GET("plants/{id}/locations")
    Call<ApiResponse<List<ApiLocation>>> getPlantLocations(@Path("id") String plantId);

    @GET("plants/{id}/parameters")
    Call<ApiResponse<List<ApiParameter>>> getPlantParameters(@Path("id") String plantId);

    @POST("readings")
    Call<ApiWriteResponse> createReading(@Body CreateReadingRequest request);

    @GET("readings/{id}")
    Call<ApiResponse<ApiReadingDetail>> getReading(@Path("id") String readingId);

    @DELETE("readings/{id}")
    Call<ApiWriteResponse> deleteReading(@Path("id") String readingId);
}
