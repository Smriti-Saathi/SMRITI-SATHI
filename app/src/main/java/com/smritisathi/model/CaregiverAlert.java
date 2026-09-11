package com.smritisathi.model;

import java.io.Serializable;

/**
 * Model representing a client-computed alert for caregivers:
 * - Score drop > 15% over 7 days
 * - Missed medication in past 24 hours
 * - Zero cognitive game sessions in 3 days
 */
public class CaregiverAlert implements Serializable {

    public enum Severity {
        CRITICAL,
        WARNING,
        INFO
    }

    private String id;
    private String title;
    private String description;
    private String patientName;
    private String alertType; // "SCORE_DROP", "MISSED_MEDICINE", "INACTIVITY"
    private Severity severity;
    private long timestamp;

    public CaregiverAlert() {
    }

    public CaregiverAlert(String id, String title, String description, String patientName,
                          String alertType, Severity severity, long timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.patientName = patientName;
        this.alertType = alertType;
        this.severity = severity;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
