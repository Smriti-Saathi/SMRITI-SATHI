const asyncHandler = require("express-async-handler");
const GameSession = require("../models/GameSession");
const PatientProfile = require("../models/PatientProfile");
const CognitiveAssessment = require("../models/CognitiveAssessment");
const {
  calculateScore,
  recommendDifficulty,
  generateRecommendation,
} = require("../services/cognitiveScoring");
const mongoose = require("mongoose");

// @desc    Record a new game session result and trigger automated cognitive assessment
// @route   POST /api/sessions
// @access  Private (Caregiver or Patient)
const createGameSession = asyncHandler(async (req, res) => {
  const {
    patientId,
    gameType,
    difficulty,
    score,
    accuracy,
    reactionTimeMs,
    durationSeconds,
  } = req.body;

  // Verify patient profile exists
  const profile = await PatientProfile.findOne({ userId: patientId });
  if (!profile) {
    res.status(404);
    throw new Error("Patient profile not found");
  }

  // Authorization: Either the logged-in user is the patient themselves, or their linked caregiver
  const isPatientSelf = req.user._id.toString() === patientId;
  const isLinkedCaregiver =
    profile.caregiverId &&
    profile.caregiverId.toString() === req.user._id.toString();

  if (!isPatientSelf && !isLinkedCaregiver) {
    res.status(403);
    throw new Error(
      "Forbidden: You are not authorized to log game sessions for this user",
    );
  }

  // 1. Create and save the game session
  const session = await GameSession.create({
    patientId,
    gameType,
    difficulty,
    score,
    accuracy,
    reactionTimeMs,
    durationSeconds,
    timestamp: new Date(),
  });

  // 2. Trigger automated background scoring & assessment creation (wrapped in try/catch for fault tolerance)
  try {
    // Fetch the patient's last 10 sessions (ordered newest first)
    const recentSessions = await GameSession.find({ patientId })
      .sort({ timestamp: -1 })
      .limit(10);

    // Calculate score using the deterministic service formula
    const scoringResult = calculateScore(recentSessions);

    if (scoringResult.status === "success") {
      const currentScore = scoringResult.score;
      const recentScores = recentSessions.map((s) => s.score);

      // Get difficulty recommendation with trend analysis override
      const difficultyRec = recommendDifficulty(currentScore, recentScores);

      // Derive category scores for recommendation generation (mapping game types to categories)
      const categoryMap = {
        objectRecall: "memory",
        faceMatching: "recognition",
        pathFinding: "attention",
        rhythmSequence: "attention",
      };

      const catScoresAccumulator = {};
      recentSessions.forEach((s) => {
        const cat = categoryMap[s.gameType] || "memory";
        if (!catScoresAccumulator[cat]) catScoresAccumulator[cat] = [];
        catScoresAccumulator[cat].push(s.accuracy);
      });

      const categoryScores = {
        memory: catScoresAccumulator.memory
          ? catScoresAccumulator.memory.reduce((a, b) => a + b, 0) /
            catScoresAccumulator.memory.length
          : currentScore,
        attention: catScoresAccumulator.attention
          ? catScoresAccumulator.attention.reduce((a, b) => a + b, 0) /
            catScoresAccumulator.attention.length
          : currentScore,
        recognition: catScoresAccumulator.recognition
          ? catScoresAccumulator.recognition.reduce((a, b) => a + b, 0) /
            catScoresAccumulator.recognition.length
          : currentScore,
      };

      const aiRecommendation = generateRecommendation(categoryScores);

      // Create the new CognitiveAssessment document
      await CognitiveAssessment.create({
        patientId,
        overallScore: currentScore,
        metrics: scoringResult.metrics,
        recommendedDifficulty: difficultyRec.level,
        recommendationReason: difficultyRec.reason,
        aiRecommendation,
        timestamp: new Date(),
      });
    }
  } catch (scoringError) {
    // Log server-side error but do not fail the user-facing session creation response
    console.error(
      "[BACKGROUND SCORING ERROR] Failed to compute automated cognitive assessment:",
      scoringError.message,
    );
  }

  // 3. Return success for the session creation immediately
  res.status(201).json({
    success: true,
    data: session,
  });
});

// @desc    Get paginated game sessions for a patient (newest first, default 20/page)
// @route   GET /api/sessions/patient/:patientId
// @access  Private (Caregiver or Patient)
const getPatientSessions = asyncHandler(async (req, res) => {
  const { patientId } = req.params;
  const page = parseInt(req.query.page, 10) || 1;
  const limit = parseInt(req.query.limit, 10) || 20;
  const skip = (page - 1) * limit;

  const profile = await PatientProfile.findOne({ userId: patientId });
  if (!profile) {
    res.status(404);
    throw new Error("Patient profile not found");
  }

  const isPatientSelf = req.user._id.toString() === patientId;
  const isLinkedCaregiver =
    profile.caregiverId &&
    profile.caregiverId.toString() === req.user._id.toString();

  if (!isPatientSelf && !isLinkedCaregiver) {
    res.status(403);
    throw new Error("Forbidden: Unauthorized to view these sessions");
  }

  const totalSessions = await GameSession.countDocuments({ patientId });
  const sessions = await GameSession.find({ patientId })
    .sort({ timestamp: -1 })
    .skip(skip)
    .limit(limit);

  res.status(200).json({
    success: true,
    pagination: {
      total: totalSessions,
      page,
      pages: Math.ceil(totalSessions / limit),
      limit,
    },
    data: sessions,
  });
});

// @desc    Get aggregated game stats using MongoDB $group pipeline
// @route   GET /api/sessions/patient/:patientId/stats?from=&to=
// @access  Private (Caregiver or Patient)
const getPatientStats = asyncHandler(async (req, res) => {
  const { patientId } = req.params;
  const { from, to } = req.query;

  const profile = await PatientProfile.findOne({ userId: patientId });
  if (!profile) {
    res.status(404);
    throw new Error("Patient profile not found");
  }

  const isPatientSelf = req.user._id.toString() === patientId;
  const isLinkedCaregiver =
    profile.caregiverId &&
    profile.caregiverId.toString() === req.user._id.toString();

  if (!isPatientSelf && !isLinkedCaregiver) {
    res.status(403);
    throw new Error("Forbidden: Unauthorized to view these stats");
  }

  const matchCriteria = { patientId: new mongoose.Types.ObjectId(patientId) };
  if (from || to) {
    matchCriteria.timestamp = {};
    if (from) matchCriteria.timestamp.$gte = new Date(from);
    if (to) matchCriteria.timestamp.$lte = new Date(to);
  }

  const statsAggregation = await GameSession.aggregate([
    { $match: matchCriteria },
    {
      $facet: {
        overallStats: [
          {
            $group: {
              _id: null,
              totalSessions: { $sum: 1 },
              avgAccuracy: { $avg: "$accuracy" },
              avgReactionTimeMs: { $avg: "$reactionTimeMs" },
            },
          },
        ],
        byGameType: [
          {
            $group: {
              _id: "$gameType",
              count: { $sum: 1 },
              avgAccuracy: { $avg: "$accuracy" },
              avgReactionTimeMs: { $avg: "$reactionTimeMs" },
            },
          },
        ],
      },
    },
  ]);

  const result = statsAggregation[0];
  const overall = result.overallStats[0] || {
    totalSessions: 0,
    avgAccuracy: 0,
    avgReactionTimeMs: 0,
  };

  const sessionsPerGameType = {};
  result.byGameType.forEach((item) => {
    sessionsPerGameType[item._id] = {
      count: item.count,
      avgAccuracy: Math.round(item.avgAccuracy * 100) / 100,
      avgReactionTimeMs: Math.round(item.avgReactionTimeMs * 100) / 100,
    };
  });

  res.status(200).json({
    success: true,
    data: {
      totalSessions: overall.totalSessions,
      avgAccuracy: Math.round((overall.avgAccuracy || 0) * 100) / 100,
      avgReactionTimeMs:
        Math.round((overall.avgReactionTimeMs || 0) * 100) / 100,
      sessionsPerGameType,
    },
  });
});

module.exports = {
  createGameSession,
  getPatientSessions,
  getPatientStats,
};
