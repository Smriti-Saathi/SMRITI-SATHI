package com.smritisathi.model;

import java.io.Serializable;

/**
 * Model representing a cognitive game session in the 'gameSessions' Firestore collection.
 */
public class GameSession implements Serializable {
    private String id;
    private String patientId;
    private String gameType; // e.g., "MemoryMatch", "PatternRecall", "WordAssociation"
    private String difficulty; // "Easy", "Medium", "Hard"
    private int score;
    private double accuracy; // Percentage 0.0 - 100.0
    private long reactionTimeMs;
    private int mistakes;
    private int durationSeconds;
    private long timestamp;

    public GameSession() {
    }

    public GameSession(String id, String patientId, String gameType, String difficulty,
                       int score, double accuracy, long reactionTimeMs, int mistakes,
                       int durationSeconds, long timestamp) {
        this.id = id;
        this.patientId = patientId;
        this.gameType = gameType;
        this.difficulty = difficulty;
        this.score = score;
        this.accuracy = accuracy;
        this.reactionTimeMs = reactionTimeMs;
        this.mistakes = mistakes;
        this.durationSeconds = durationSeconds;
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

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public long getReactionTimeMs() {
        return reactionTimeMs;
    }

    public void setReactionTimeMs(long reactionTimeMs) {
        this.reactionTimeMs = reactionTimeMs;
    }

    public int getMistakes() {
        return mistakes;
    }

    public void setMistakes(int mistakes) {
        this.mistakes = mistakes;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
