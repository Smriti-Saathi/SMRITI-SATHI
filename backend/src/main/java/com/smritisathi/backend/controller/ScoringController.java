package com.smritisathi.backend.controller;

import com.smritisathi.backend.dto.GameSessionDto;
import com.smritisathi.backend.dto.ScoreResultDto;
import com.smritisathi.backend.service.ScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller providing cognitive score evaluation and difficulty adaptation.
 */
@RestController
@RequestMapping("/api")
public class ScoringController {

    private final ScoringService scoringService;

    public ScoringController(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    /**
     * Evaluates a list of recent game sessions and returns an overall cognitive score,
     * recommended difficulty level, and clinical explanation.
     *
     * @param sessions List of recent session data ({accuracy, reactionTimeMs, gameType})
     * @return ScoreResultDto ({overallScore, recommendedDifficulty, reasonText})
     */
    @PostMapping("/score")
    public ResponseEntity<ScoreResultDto> evaluateScore(@RequestBody List<GameSessionDto> sessions) {
        ScoreResultDto result = scoringService.calculateScore(sessions);
        return ResponseEntity.ok(result);
    }
}
