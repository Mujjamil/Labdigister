package com.labdigitiser;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.labdigitiser.databinding.ActivitySignupBinding;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.createAccountButton.setOnClickListener(v -> attemptSignup());

        binding.goToLogin.setOnClickListener(v -> finish());
    }

    private void attemptSignup() {
        String fullName = getValue(binding.signupFullName);
        String industry = getValue(binding.signupIndustry);
        String email = getValue(binding.signupEmail);
        String password = getValue(binding.signupPassword);
        String confirmPassword = getValue(binding.signupConfirmPassword);

        if (TextUtils.isEmpty(fullName)) {
            binding.signupFullName.setError("Enter full name");
            binding.signupFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(industry)) {
            binding.signupIndustry.setError("Enter industry / board name");
            binding.signupIndustry.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            binding.signupEmail.setError("Enter email");
            binding.signupEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.signupPassword.setError("Minimum 6 characters");
            binding.signupPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.signupConfirmPassword.setError("Passwords do not match");
            binding.signupConfirmPassword.requestFocus();
            return;
        }

        binding.createAccountButton.setEnabled(false);
        FirebaseManager.signUp(email, password, task -> {
            if (!task.isSuccessful()) {
                binding.createAccountButton.setEnabled(true);
                Toast.makeText(
                        this,
                        task.getException() != null ? task.getException().getMessage() : "Sign up failed",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            String organizationId = FirebaseManager.toOrganizationId(industry);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("fullName", fullName);
            userMap.put("industry", industry);
            userMap.put("email", email);
            userMap.put("organizationId", organizationId);
            userMap.put("role", "operator");
            userMap.put("active", true);
            userMap.put("createdAt", System.currentTimeMillis());

            FirebaseManager.saveUserProfile(userMap, profileTask -> {
                if (!profileTask.isSuccessful()) {
                    binding.createAccountButton.setEnabled(true);
                    Toast.makeText(
                            this,
                            profileTask.getException() != null ? profileTask.getException().getMessage() : "Profile save failed",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                FirebaseManager.ensureOrganizationProfile(organizationId, industry, orgTask -> {
                    binding.createAccountButton.setEnabled(true);

                    if (!orgTask.isSuccessful()) {
                        Toast.makeText(
                                this,
                                orgTask.getException() != null ? orgTask.getException().getMessage() : "Organization setup failed",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    FirebaseManager.signOut();

                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    loginIntent.putExtra(LoginActivity.EXTRA_PREFILL_EMAIL, email);
                    loginIntent.putExtra(LoginActivity.EXTRA_SIGNUP_SUCCESS, true);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    finish();
                });
            });
        });
    }

    private String getValue(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
