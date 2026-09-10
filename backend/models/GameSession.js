const mongoose = require("mongoose");

const gameSessionSchema = new mongoose.Schema({
  patientId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    required: [true, "Patient ID is required"],
    index: true, // Individual index for searching all games by a patient
  },
  gameType: {
    type: String,
    enum: ["memoryMatch", "objectRecall", "patternRecognition"],
    required: [true, "Game type is required"],
  },
  difficulty: {
    type: String,
    enum: ["easy", "moderate", "difficult"],
    required: [true, "Difficulty level is required"],
  },
  score: {
    type: Number,
    required: [true, "Score is required"],
    min: [0, "Score cannot be less than 0"],
    max: [100, "Score cannot exceed 100"],
  },
  accuracy: {
    type: Number,
    required: [true, "Accuracy is required"],
    min: [0, "Accuracy cannot be less than 0"],
    max: [100, "Accuracy cannot exceed 100"],
  },
  reactionTimeMs: {
    type: Number,
    required: [true, "Reaction time is required"],
  },
  mistakes: {
    type: Number,
    default: 0,
  },
  durationSeconds: {
    type: Number,
    required: [true, "Duration in seconds is required"],
  },
  timestamp: {
    type: Date,
    default: Date.now,
    index: true, // Individual index for searching games by date across all users
  },
});

// Compound index to optimize the Caregiver Analytics Dashboard queries
// (e.g., Fetching a specific patient's recent game history rapidly)
gameSessionSchema.index({ patientId: 1, timestamp: -1 });

module.exports = mongoose.model("GameSession", gameSessionSchema);
