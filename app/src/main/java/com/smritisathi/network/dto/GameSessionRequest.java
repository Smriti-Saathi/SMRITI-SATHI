package com.smritisathi.network.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Request payload representing a game session sent to the Spring Boot scoring service.
 */
public class GameSessionRequest implements Serializable {

    @SerializedName("accuracy")
    private double accuracy;

    @SerializedName("reactionTimeMs")
    private long reactionTimeMs;

    @SerializedName("gameType")
    private String gameType;

    public GameSessionRequest() {
    }

    public GameSessionRequest(double accuracy, long reactionTimeMs, String gameType) {
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
}
