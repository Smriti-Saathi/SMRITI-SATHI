package com.smritisathi.backend.dto;

import java.io.Serializable;

/**
 * Data Transfer Object representing the calculated score assessment and recommended difficulty.
 */
public class ScoreResultDto implements Serializable {

    private double overallScore;
    private String recommendedDifficulty; // "difficult", "moderate", "easy"
    private String reasonText;

    public ScoreResultDto() {
    }

    public ScoreResultDto(double overallScore, String recommendedDifficulty, String reasonText) {
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
        return "ScoreResultDto{" +
                "overallScore=" + overallScore +
                ", recommendedDifficulty='" + recommendedDifficulty + '\'' +
                ", reasonText='" + reasonText + '\'' +
                '}';
    }
}
