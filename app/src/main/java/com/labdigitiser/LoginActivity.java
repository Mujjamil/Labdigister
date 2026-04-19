package com.labdigitiser;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.labdigitiser.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_PREFILL_EMAIL = "prefill_email";
    public static final String EXTRA_SIGNUP_SUCCESS = "signup_success";

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        handleIncomingState();

        if (FirebaseManager.isLoggedIn()) {
            openMain();
            return;
        }

        binding.loginButton.setOnClickListener(v -> attemptLogin());

        binding.goToSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));
    }

    private void handleIncomingState() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String prefillEmail = intent.getStringExtra(EXTRA_PREFILL_EMAIL);
        if (!TextUtils.isEmpty(prefillEmail)) {
            binding.loginEmail.setText(prefillEmail);
        }

        if (intent.getBooleanExtra(EXTRA_SIGNUP_SUCCESS, false)) {
            Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_LONG).show();
        }
    }

    private void attemptLogin() {
        String email = binding.loginEmail.getText() == null
                ? ""
                : binding.loginEmail.getText().toString().trim();
        String password = binding.loginPassword.getText() == null
                ? ""
                : binding.loginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.loginEmail.setError("Enter email");
            binding.loginEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.loginPassword.setError("Enter password");
            binding.loginPassword.requestFocus();
            return;
        }

        binding.loginButton.setEnabled(false);
        FirebaseManager.signIn(email, password, task -> {
            binding.loginButton.setEnabled(true);

            if (task.isSuccessful()) {
                openMain();
            } else {
                Toast.makeText(
                        this,
                        task.getException() != null ? task.getException().getMessage() : "Login failed",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
