const GameSession = require("../models/GameSession");
const PatientProfile = require("../models/PatientProfile");

exports.createGameSession = async (req, res) => {
  try {
    const {
      patientId,
      gameType,
      difficulty,
      score,
      accuracy,
      reactionTimeMs,
      durationSeconds,
    } = req.body;

    // 1. Save the game session to the database
    const newSession = new GameSession({
      patientId,
      gameType,
      difficulty,
      score,
      accuracy,
      reactionTimeMs,
      durationSeconds,
      createdAt: req.body.createdAt || new Date(),
    });

    await newSession.save();

    // 2. Calculate the streak logic
    const patientProfile = await PatientProfile.findOne({ userId: patientId });

    if (patientProfile) {
      const todayStr = new Date().toISOString().split("T")[0]; // Today's date (YYYY-MM-DD)
      const lastPlayedStr = patientProfile.lastPlayedDate
        ? new Date(patientProfile.lastPlayedDate).toISOString().split("T")[0]
        : null;

      // Only update streak if it's a new calendar day
      if (lastPlayedStr !== todayStr) {
        const yesterday = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        const yesterdayStr = yesterday.toISOString().split("T")[0];

        if (lastPlayedStr === yesterdayStr) {
          // They played yesterday! Increment streak by 1
          patientProfile.streakCount += 1;
        } else {
          // They missed a day or this is their first time playing.
          // Reset to 1 (never 0, so we avoid discouraging language!)
          patientProfile.streakCount = 1;
        }

        patientProfile.lastPlayedDate = new Date();
        await patientProfile.save();
      }
    }

    return res.status(201).json({
      success: true,
      message: "Game session recorded and streak updated!",
      data: newSession,
      currentStreak: patientProfile ? patientProfile.streakCount : 1,
    });
  } catch (error) {
    console.error("Error recording game session:", error);
    return res.status(500).json({ success: false, message: "Server error" });
  }
};
