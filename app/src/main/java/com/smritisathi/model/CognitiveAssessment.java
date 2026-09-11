package com.smritisathi.model;

import java.io.Serializable;

/**
 * Model representing a cognitive health assessment in the 'cognitiveAssessments' Firestore collection.
 */
public class CognitiveAssessment implements Serializable {
    private String id;
    private String patientId;
    private double memoryScore;
    private double attentionScore;
    private double recognitionScore;
    private double overallScore;
    private String recommendedDifficulty; // "Easy", "Medium", "Hard"
    private long timestamp;

    public CognitiveAssessment() {
    }

    public CognitiveAssessment(String id, String patientId, double memoryScore,
                               double attentionScore, double recognitionScore,
                               double overallScore, String recommendedDifficulty,
                               long timestamp) {
        this.id = id;
        this.patientId = patientId;
        this.memoryScore = memoryScore;
        this.attentionScore = attentionScore;
        this.recognitionScore = recognitionScore;
        this.overallScore = overallScore;
        this.recommendedDifficulty = recommendedDifficulty;
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

    public double getMemoryScore() {
        return memoryScore;
    }

    public void setMemoryScore(double memoryScore) {
        this.memoryScore = memoryScore;
    }

    public double getAttentionScore() {
        return attentionScore;
    }

    public void setAttentionScore(double attentionScore) {
        this.attentionScore = attentionScore;
    }

    public double getRecognitionScore() {
        return recognitionScore;
    }

    public void setRecognitionScore(double recognitionScore) {
        this.recognitionScore = recognitionScore;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public String getRecommendedDifficulty() {
        return recommendedDifficulty;
    }

    public void setRecommendedDifficulty(String recommendedDifficulty) {
        this.recommendedDifficulty = recommendedDifficulty;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
