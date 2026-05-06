package com.labdigitiser;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.labdigitiser.databinding.ActivityLoginBinding;
import com.labdigitiser.network.WebsiteRepository;
import com.labdigitiser.network.model.ApiLoginData;
import com.labdigitiser.network.model.ApiPlant;
import com.labdigitiser.network.model.ApiResponse;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private WebsiteRepository websiteRepository;
    private boolean isPasswordVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        websiteRepository = new WebsiteRepository(this);

        if (websiteRepository.hasActiveSession()) {
            openMain();
            return;
        }

        bindPasswordToggle();
        binding.loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void bindPasswordToggle() {
        binding.loginPasswordToggle.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        int selection = binding.loginPassword.getSelectionEnd();
        binding.loginPassword.setTransformationMethod(
                isPasswordVisible
                        ? HideReturnsTransformationMethod.getInstance()
                        : PasswordTransformationMethod.getInstance()
        );
        binding.loginPassword.setSelection(Math.max(selection, 0));
    }

    private void attemptLogin() {
        String username = binding.loginEmail.getText() == null
                ? ""
                : binding.loginEmail.getText().toString().trim();
        String password = binding.loginPassword.getText() == null
                ? ""
                : binding.loginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            binding.loginEmail.setError("Enter username");
            binding.loginEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.loginPassword.setError("Enter password");
            binding.loginPassword.requestFocus();
            return;
        }

        binding.loginButton.setEnabled(false);
        websiteRepository.login(username, password).enqueue(new retrofit2.Callback<ApiResponse<ApiLoginData>>() {
            @Override
            public void onResponse(
                    retrofit2.Call<ApiResponse<ApiLoginData>> call,
                    retrofit2.Response<ApiResponse<ApiLoginData>> response
            ) {
                binding.loginButton.setEnabled(true);

                ApiResponse<ApiLoginData> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    String message = body != null ? body.getReadableMessage() : "Login failed";
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                    return;
                }

                ApiLoginData loginData = body.getData();
                if (loginData.getUser() == null || !"member".equalsIgnoreCase(loginData.getUser().getRole())) {
                    websiteRepository.clearSession();
                    Toast.makeText(
                            LoginActivity.this,
                            "This app is for member accounts only. Admin should use the website dashboard.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                websiteRepository.saveAuthToken(loginData.getToken());
                saveDefaultPlant(loginData.getPlants());
                openMain();
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<ApiLoginData>> call, Throwable t) {
                binding.loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveDefaultPlant(List<ApiPlant> plants) {
        if (plants == null || plants.isEmpty()) {
            return;
        }

        ApiPlant firstPlant = plants.get(0);
        websiteRepository.selectPlant(String.valueOf(firstPlant.getId()), firstPlant.getPlantName());
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
