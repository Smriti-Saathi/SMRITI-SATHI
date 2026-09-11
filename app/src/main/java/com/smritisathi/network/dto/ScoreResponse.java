package com.smritisathi.network.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Response payload returned from the Spring Boot scoring service.
 */
public class ScoreResponse implements Serializable {

    @SerializedName("overallScore")
    private double overallScore;

    @SerializedName("recommendedDifficulty")
    private String recommendedDifficulty; // "difficult", "moderate", "easy"

    @SerializedName("reasonText")
    private String reasonText;

    public ScoreResponse() {
    }

    public ScoreResponse(double overallScore, String recommendedDifficulty, String reasonText) {
        this.overallScore = overallScore;
        this.recommendedDifficulty = recommendedDifficulty;
        this.reasonText = reasonText;
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

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    @Override
    public String toString() {
        return "ScoreResponse{" +
                "overallScore=" + overallScore +
                ", recommendedDifficulty='" + recommendedDifficulty + '\'' +
                ", reasonText='" + reasonText + '\'' +
                '}';
    }
}
