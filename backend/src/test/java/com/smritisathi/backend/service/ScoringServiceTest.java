package com.smritisathi.backend.service;

import com.smritisathi.backend.dto.GameSessionDto;
import com.smritisathi.backend.dto.ScoreResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoringServiceTest {

    private ScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringService();
    }

    @Test
    void testHighPerformanceYieldsDifficult() {
        List<GameSessionDto> sessions = new ArrayList<>();
        sessions.add(new GameSessionDto(95.0, 600, "MemoryMatch"));
        sessions.add(new GameSessionDto(90.0, 750, "simonSays"));
        sessions.add(new GameSessionDto(92.0, 700, "PatternRecognition"));

        ScoreResultDto result = scoringService.calculateScore(sessions);

        assertNotNull(result);
        assertTrue(result.getOverallScore() > 75.0, "Score should exceed 75.0 for high performance");
        assertEquals("difficult", result.getRecommendedDifficulty());
        assertTrue(result.getReasonText().contains("difficult tier"));
    }

    @Test
    void testLowPerformanceYieldsEasy() {
        List<GameSessionDto> sessions = new ArrayList<>();
        sessions.add(new GameSessionDto(15.0, 5500, "ObjectRecall"));
        sessions.add(new GameSessionDto(10.0, 6000, "simonSays"));
        sessions.add(new GameSessionDto(20.0, 5200, "MemoryMatch"));

        ScoreResultDto result = scoringService.calculateScore(sessions);

        assertNotNull(result);
        assertTrue(result.getOverallScore() < 45.0, "Score should be under 45.0 for low performance");
        assertEquals("easy", result.getRecommendedDifficulty());
        assertTrue(result.getReasonText().contains("easy tier"));
    }

    @Test
    void testBalancedPerformanceYieldsModerate() {
        List<GameSessionDto> sessions = new ArrayList<>();
        sessions.add(new GameSessionDto(60.0, 2500, "MemoryMatch"));
        sessions.add(new GameSessionDto(55.0, 2800, "ObjectRecall"));
        sessions.add(new GameSessionDto(65.0, 2400, "simonSays"));

        ScoreResultDto result = scoringService.calculateScore(sessions);

        assertNotNull(result);
        assertTrue(result.getOverallScore() >= 45.0 && result.getOverallScore() <= 75.0,
                "Score should be between 45.0 and 75.0 for balanced performance");
        assertEquals("moderate", result.getRecommendedDifficulty());
        assertTrue(result.getReasonText().contains("Moderate tier"));
    }

    @Test
    void testEmptySessionListReturnsSafeBaseline() {
        ScoreResultDto result = scoringService.calculateScore(Collections.emptyList());

        assertNotNull(result);
        assertEquals(50.0, result.getOverallScore());
        assertEquals("moderate", result.getRecommendedDifficulty());
    }
}
