package com.smritisathi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smritisathi.R;
import com.smritisathi.data.FirestoreHelper;
import com.smritisathi.data.SessionManager;
import com.smritisathi.model.User;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private ProgressBar pbLoading;
    private TextView tvError;
    private TextView tvGoToRegister;

    private FirebaseAuth mAuth;
    private FirestoreHelper firestoreHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        firestoreHelper = FirestoreHelper.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        pbLoading = findViewById(R.id.pbLoginLoading);
        tvError = findViewById(R.id.tvLoginError);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Validations
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            fetchUserProfile(firebaseUser.getUid(), firebaseUser.getEmail());
                        } else {
                            setLoading(false);
                            showError("Authentication succeeded but user profile was not found.");
                        }
                    } else {
                        setLoading(false);
                        String errorMsg = task.getException() != null ?
                                task.getException().getLocalizedMessage() : "Login failed. Please check your credentials.";
                        showError(errorMsg);
                    }
                });
    }

    private void fetchUserProfile(String uid, String email) {
        firestoreHelper.getUser(uid).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                User user = task.getResult().toObject(User.class);
                if (user != null) {
                    sessionManager.saveUser(user);
                } else {
                    sessionManager.saveUserSession(uid, "patient", "User", email, "");
                }
            } else {
                // If Firestore record is missing or offline, fallback to basic session
                sessionManager.saveUserSession(uid, "patient", "User", email, "");
            }

            Toast.makeText(LoginActivity.this, "Welcome to Smriti Sathi!", Toast.LENGTH_SHORT).show();
            Intent intent;
            if (sessionManager.isPatient()) {
                intent = new Intent(LoginActivity.this, PatientDashboardActivity.class);
            } else if (sessionManager.isCaregiver()) {
                intent = new Intent(LoginActivity.this, CaregiverDashboardActivity.class);
            } else {
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        if (isLoading) {
            tvError.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
