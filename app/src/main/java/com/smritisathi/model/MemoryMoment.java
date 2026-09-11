package com.smritisathi.model;

import java.io.Serializable;

/**
 * Model representing a family or personal memory moment in the 'memoryMoments' Firestore collection.
 */
public class MemoryMoment implements Serializable {
    private String id;
    private String patientId;
    private String imageUrl;
    private String personName;
    private String relationship; // e.g. "Daughter", "Son", "Spouse", "Grandchild", "Friend"
    private String description;
    private String createdBy; // UID of creator (caregiver or patient)
    private long timestamp;

    public MemoryMoment() {
    }

    public MemoryMoment(String id, String patientId, String imageUrl, String personName,
                        String relationship, String description, String createdBy, long timestamp) {
        this.id = id;
        this.patientId = patientId;
        this.imageUrl = imageUrl;
        this.personName = personName;
        this.relationship = relationship;
        this.description = description;
        this.createdBy = createdBy;
        this.timestamp = timestamp;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
