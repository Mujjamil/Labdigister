package com.labdigitiser.network;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessionStore sessionStore;

    public AuthInterceptor(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String token = sessionStore.getAuthToken();

        if (token == null || token.trim().isEmpty()) {
            return chain.proceed(originalRequest);
        }

        Request requestWithAuth = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token.trim())
                .build();

        return chain.proceed(requestWithAuth);
    }
}
