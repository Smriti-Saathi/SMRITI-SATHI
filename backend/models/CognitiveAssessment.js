const mongoose = require("mongoose");

const cognitiveAssessmentSchema = new mongoose.Schema(
  {
    patientId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: [true, "Patient ID is required"],
      index: true, // Individual index
    },
    memoryScore: {
      type: Number,
      min: [0, "Score cannot be less than 0"],
      max: [100, "Score cannot exceed 100"],
      default: 0,
    },
    attentionScore: {
      type: Number,
      min: [0, "Score cannot be less than 0"],
      max: [100, "Score cannot exceed 100"],
      default: 0,
    },
    recognitionScore: {
      type: Number,
      min: [0, "Score cannot be less than 0"],
      max: [100, "Score cannot exceed 100"],
      default: 0,
    },
    overallScore: {
      type: Number,
      min: [0, "Score cannot be less than 0"],
      max: [100, "Score cannot exceed 100"],
      default: 0,
    },
    recommendedDifficulty: {
      type: String,
      enum: {
        values: ["easy", "moderate", "difficult"],
        message: "{VALUE} is not a valid difficulty level",
      },
    },
    recommendationText: {
      type: String,
    },
    timestamp: {
      type: Date,
      default: Date.now,
    },
  },
  {
    timestamps: true, // Adds createdAt and updatedAt automatically
  },
);

// Compound index for AI/ML algorithm queries
// (e.g., "Get the latest assessment for Patient X to set current game difficulty")
cognitiveAssessmentSchema.index({ patientId: 1, timestamp: -1 });

module.exports = mongoose.model(
  "CognitiveAssessment",
  cognitiveAssessmentSchema,
);
