package com.smritisathi.model;

import java.io.Serializable;

/**
 * Model representing a user in the 'users' Firestore collection.
 * Roles: "patient" or "caregiver".
 */
public class User implements Serializable {
    private String uid;
    private String name;
    private String email;
    private int age;
    private String role; // "patient" or "caregiver"
    private String phone;
    private String preferredLanguage;
    private String region;
    private String caregiverId; // nullable, used to link caregiver to patient
    private String inviteCode; // 6-character code if role is patient, null/empty otherwise
    private long createdAt;
    private long lastActive;

    // Required empty constructor for Firestore deserialization
    public User() {
    }

    public User(String uid, String name, String email, int age, String role,
                String phone, String preferredLanguage, String region,
                String caregiverId, String inviteCode, long createdAt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.age = age;
        this.role = role;
        this.phone = phone;
        this.preferredLanguage = preferredLanguage;
        this.region = region;
        this.caregiverId = caregiverId;
        this.inviteCode = inviteCode;
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCaregiverId() {
        return caregiverId;
    }

    public void setCaregiverId(String caregiverId) {
        this.caregiverId = caregiverId;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    public boolean isPatient() {
        return "patient".equalsIgnoreCase(role);
    }

    public boolean isCaregiver() {
        return "caregiver".equalsIgnoreCase(role);
    }
}
