package com.smritisathi.model;

import java.io.Serializable;

/**
 * Model representing a reminder or medication alert in the 'reminders' Firestore collection.
 */
public class Reminder implements Serializable {
    private String id;
    private String patientId;
    private String type; // e.g. "Medication", "Hydration", "Meal", "DoctorAppointment"
    private String title;
    private long scheduledTime;
    private String recurrence; // "Daily", "Weekly", "Once", "Custom"
    private String status; // "Pending", "Taken", "Missed", "Completed"
    private String medicineName;
    private String dosage;

    public Reminder() {
    }

    public Reminder(String id, String patientId, String type, String title,
                    long scheduledTime, String recurrence, String status,
                    String medicineName, String dosage) {
        this.id = id;
        this.patientId = patientId;
        this.type = type;
        this.title = title;
        this.scheduledTime = scheduledTime;
        this.recurrence = recurrence;
        this.status = status;
        this.medicineName = medicineName;
        this.dosage = dosage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
}
