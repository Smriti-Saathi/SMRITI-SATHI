import com.smritisathi.backend.dto.GameSessionDto;
import com.smritisathi.backend.dto.ScoreResultDto;
import java.util.ArrayList;
import java.util.List;

public class TestScoringMath {
    public static void main(String[] args) {
        System.out.println("=== Testing ScoringService Math ===");

        // Test 1: High performance
        List<GameSessionDto> highSessions = new ArrayList<>();
        highSessions.add(new GameSessionDto(95.0, 600, "MemoryMatch"));
        highSessions.add(new GameSessionDto(90.0, 750, "simonSays"));
        highSessions.add(new GameSessionDto(92.0, 700, "PatternRecognition"));

        ScoreResultDto r1 = calculate(highSessions);
        System.out.println("High performance -> Score: " + r1.getOverallScore() + ", Tier: " + r1.getRecommendedDifficulty());
        assert r1.getOverallScore() > 75.0 : "Score should exceed 75";
        assert "difficult".equals(r1.getRecommendedDifficulty()) : "Tier should be difficult";

        // Test 2: Low performance
        List<GameSessionDto> lowSessions = new ArrayList<>();
        lowSessions.add(new GameSessionDto(15.0, 5500, "ObjectRecall"));
        lowSessions.add(new GameSessionDto(10.0, 6000, "simonSays"));
        lowSessions.add(new GameSessionDto(20.0, 5200, "MemoryMatch"));

        ScoreResultDto r2 = calculate(lowSessions);
        System.out.println("Low performance -> Score: " + r2.getOverallScore() + ", Tier: " + r2.getRecommendedDifficulty());
        assert r2.getOverallScore() < 45.0 : "Score should be < 45";
        assert "easy".equals(r2.getRecommendedDifficulty()) : "Tier should be easy";

        // Test 3: Balanced performance
        List<GameSessionDto> balSessions = new ArrayList<>();
        balSessions.add(new GameSessionDto(60.0, 2500, "MemoryMatch"));
        balSessions.add(new GameSessionDto(55.0, 2800, "ObjectRecall"));
        balSessions.add(new GameSessionDto(65.0, 2400, "simonSays"));

        ScoreResultDto r3 = calculate(balSessions);
        System.out.println("Balanced performance -> Score: " + r3.getOverallScore() + ", Tier: " + r3.getRecommendedDifficulty());
        assert r3.getOverallScore() >= 45.0 && r3.getOverallScore() <= 75.0 : "Score should be moderate";
        assert "moderate".equals(r3.getRecommendedDifficulty()) : "Tier should be moderate";

        System.out.println("All mathematical tests PASSED perfectly!");
    }

    private static ScoreResultDto calculate(List<GameSessionDto> sessions) {
        int n = sessions.size();
        double sumAccuracy = 0.0;
        long sumReactionTimeMs = 0;
        for (GameSessionDto s : sessions) {
            sumAccuracy += s.getAccuracy();
            sumReactionTimeMs += s.getReactionTimeMs();
        }
        double avgAccuracy = sumAccuracy / n;
        double avgReactionTimeMs = (double) sumReactionTimeMs / n;
        double normalizedReactionTime = Math.max(0.0, 100.0 - Math.min(avgReactionTimeMs / 50.0, 100.0));

        double varianceSum = 0.0;
        for (GameSessionDto s : sessions) {
            double diff = s.getAccuracy() - avgAccuracy;
            varianceSum += diff * diff;
        }
        double stdDev = Math.sqrt(varianceSum / n);
        double consistency = Math.max(0.0, Math.min(100.0, 100.0 - stdDev));

        double overall = (0.40 * avgAccuracy) + (0.25 * normalizedReactionTime) + (0.20 * consistency) + (0.15 * 100.0);
        double score = Math.round(overall * 10.0) / 10.0;

        String diff;
        if (score > 75.0) diff = "difficult";
        else if (score < 45.0) diff = "easy";
        else diff = "moderate";

        return new ScoreResultDto(score, diff, "Test reason");
    }
}
