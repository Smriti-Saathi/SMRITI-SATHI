package com.smritisathi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private TextInputEditText etAge;
    private TextInputEditText etPhone;
    private RadioGroup rgRole;
    private RadioButton rbRolePatient;
    private RadioButton rbRoleCaregiver;
    private Spinner spnLanguage;
    private TextInputEditText etRegion;
    private TextInputEditText etCaregiverId;
    private MaterialButton btnRegister;
    private ProgressBar pbLoading;
    private TextView tvError;
    private TextView tvGoToLogin;

    private FirebaseAuth mAuth;
    private FirestoreHelper firestoreHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firestoreHelper = FirestoreHelper.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupLanguageSpinner();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etRegisterName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        etAge = findViewById(R.id.etRegisterAge);
        etPhone = findViewById(R.id.etRegisterPhone);
        rgRole = findViewById(R.id.rgRole);
        rbRolePatient = findViewById(R.id.rbRolePatient);
        rbRoleCaregiver = findViewById(R.id.rbRoleCaregiver);
        spnLanguage = findViewById(R.id.spnPreferredLanguage);
        etRegion = findViewById(R.id.etRegisterRegion);
        etCaregiverId = findViewById(R.id.etCaregiverId);
        btnRegister = findViewById(R.id.btnRegister);
        pbLoading = findViewById(R.id.pbRegisterLoading);
        tvError = findViewById(R.id.tvRegisterError);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
    }

    private void setupLanguageSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.languages_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLanguage.setAdapter(adapter);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";
        String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String region = etRegion.getText() != null ? etRegion.getText().toString().trim() : "";
        String caregiverId = etCaregiverId.getText() != null ? etCaregiverId.getText().toString().trim() : null;
        String preferredLanguage = spnLanguage.getSelectedItem() != null ? spnLanguage.getSelectedItem().toString() : "English";

        final String role = rbRolePatient.isChecked() ? "patient" : "caregiver";

        // Input validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Full name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        int age = 0;
        if (!TextUtils.isEmpty(ageStr)) {
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ignored) {
            }
        }

        setLoading(true);

        final int finalAge = age;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            // Generate random 6-character invite code if user is a patient
                            String inviteCode = null;
                            if ("patient".equalsIgnoreCase(role)) {
                                inviteCode = FirestoreHelper.generateInviteCode();
                            }

                            User user = new User(
                                    uid,
                                    name,
                                    email,
                                    finalAge,
                                    role,
                                    phone,
                                    preferredLanguage,
                                    region,
                                    caregiverId,
                                    inviteCode,
                                    System.currentTimeMillis()
                            );

                            saveUserToFirestoreAndComplete(user);
                        } else {
                            setLoading(false);
                            showError("User creation succeeded but UID was not returned.");
                        }
                    } else {
                        setLoading(false);
                        String errorMsg = task.getException() != null ?
                                task.getException().getLocalizedMessage() : "Registration failed.";
                        showError(errorMsg);
                    }
                });
    }

    private void saveUserToFirestoreAndComplete(User user) {
        firestoreHelper.saveUser(user).addOnCompleteListener(task -> {
            setLoading(false);
            Intent intent;
            if (user.isPatient()) {
                intent = new Intent(RegisterActivity.this, PatientDashboardActivity.class);
            } else if (user.isCaregiver()) {
                intent = new Intent(RegisterActivity.this, CaregiverDashboardActivity.class);
            } else {
                intent = new Intent(RegisterActivity.this, MainActivity.class);
            }

            if (task.isSuccessful()) {
                // Save complete session locally
                sessionManager.saveUser(user);

                Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                // Even if Firestore write is delayed or encounters permission/offline issues, save session
                sessionManager.saveUser(user);
                Toast.makeText(RegisterActivity.this, "Account created with local session!", Toast.LENGTH_SHORT).show();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
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
