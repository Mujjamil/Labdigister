package com.labdigitiser.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.labdigitiser.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static Retrofit retrofit;

    private ApiClient() {
    }

    public static LabDigitiserApi getService(Context context) {
        if (retrofit == null) {
            SessionStore sessionStore = new SessionStore(context);

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(sessionStore))
                    .addInterceptor(loggingInterceptor)
                    .build();

            Gson gson = new GsonBuilder().create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.LABDIGITISER_BASE_URL)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit.create(LabDigitiserApi.class);
    }
}
