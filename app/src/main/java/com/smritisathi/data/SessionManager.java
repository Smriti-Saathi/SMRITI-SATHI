package com.smritisathi.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.smritisathi.model.User;

/**
 * Manages user session state using SharedPreferences.
 * Stores logged-in UID, role (patient/caregiver), profile information, and login flag.
 */
public class SessionManager {

    private static final String PREF_NAME = "smriti_sathi_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_ROLE = "user_role"; // "patient" or "caregiver"
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_INVITE_CODE = "invite_code";
    private static final String KEY_PHONE = "user_phone";
    private static final String KEY_PREFERRED_LANGUAGE = "preferred_language";
    private static final String KEY_REGION = "user_region";
    private static final String KEY_LINKED_PATIENT_ID = "linked_patient_id";
    private static final String KEY_LINKED_PATIENT_NAME = "linked_patient_name";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        this.pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
    }

    /**
     * Saves user session data upon successful login or registration.
     */
    public void saveUserSession(String uid, String role, String name, String email, String inviteCode) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, uid);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_INVITE_CODE, inviteCode != null ? inviteCode : "");
        editor.apply();
    }

    /**
     * Saves complete User profile to session.
     */
    public void saveUser(User user) {
        if (user == null) return;
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getUid());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_INVITE_CODE, user.getInviteCode() != null ? user.getInviteCode() : "");
        editor.putString(KEY_PHONE, user.getPhone() != null ? user.getPhone() : "");
        editor.putString(KEY_PREFERRED_LANGUAGE, user.getPreferredLanguage() != null ? user.getPreferredLanguage() : "");
        editor.putString(KEY_REGION, user.getRegion() != null ? user.getRegion() : "");
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, "");
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "User");
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public String getInviteCode() {
        return pref.getString(KEY_INVITE_CODE, "");
    }

    public String getPhone() {
        return pref.getString(KEY_PHONE, "");
    }

    public String getPreferredLanguage() {
        return pref.getString(KEY_PREFERRED_LANGUAGE, "English");
    }

    public String getRegion() {
        return pref.getString(KEY_REGION, "");
    }

    public void saveLinkedPatient(String patientId, String patientName) {
        editor.putString(KEY_LINKED_PATIENT_ID, patientId);
        editor.putString(KEY_LINKED_PATIENT_NAME, patientName != null ? patientName : "");
        editor.apply();
    }

    public String getLinkedPatientId() {
        return pref.getString(KEY_LINKED_PATIENT_ID, null);
    }

    public String getLinkedPatientName() {
        return pref.getString(KEY_LINKED_PATIENT_NAME, "My Patient");
    }

    public boolean hasLinkedPatient() {
        return getLinkedPatientId() != null && !getLinkedPatientId().isEmpty();
    }

    public boolean isPatient() {
        return "patient".equalsIgnoreCase(getUserRole());
    }

    public boolean isCaregiver() {
        return "caregiver".equalsIgnoreCase(getUserRole());
    }

    /**
     * Clears all session data and signs out from Firebase.
     */
    public void logout() {
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception ignored) {
        }
        editor.clear();
        editor.apply();
    }
}
