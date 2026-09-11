package com.smritisathi.backend.dto;

import java.io.Serializable;

/**
 * Data Transfer Object representing a game session submitted for scoring.
 * Valid gameTypes: "MemoryMatch", "ObjectRecall", "PatternRecognition", "simonSays".
 */
public class GameSessionDto implements Serializable {

    private double accuracy; // Percentage 0.0 - 100.0
    private long reactionTimeMs; // Reaction time in milliseconds
    private String gameType; // e.g. "MemoryMatch", "ObjectRecall", "PatternRecognition", "simonSays"

    public GameSessionDto() {
    }

    public GameSessionDto(double accuracy, long reactionTimeMs, String gameType) {
        this.accuracy = accuracy;
        this.reactionTimeMs = reactionTimeMs;
        this.gameType = gameType;
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

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    @Override
    public String toString() {
        return "GameSessionDto{" +
                "accuracy=" + accuracy +
                ", reactionTimeMs=" + reactionTimeMs +
                ", gameType='" + gameType + '\'' +
                '}';
    }
}
