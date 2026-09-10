/*
 * CAREGIVER-PATIENT LINKING APPROACH:
 * 1. Caregiver enters the 6-char inviteCode via a mobile screen.
 * 2. Backend finds the matching PatientProfile and sets its caregiverId.
 * 3. MVP RESTRICTION: 1:1 Caregiver to Patient limit.
 * 4. If caregiverId is already set, return 400 error: "Patient already linked to a caregiver".
 * 5. Code remains reusable in theory, but restricted by the 1:1 MVP constraint.
 */
const mongoose = require("mongoose");

const PatientProfileSchema = new mongoose.Schema({
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    required: true,
    unique: true,
  },
  name: {
    type: String,
    required: true,
  },
  age: {
    type: Number,
    required: true,
  },
  preferredLanguage: {
    type: String,
    enum: ["en", "as"],
    default: "en",
  },
  // Gamification Streak Fields
  streakCount: {
    type: Number,
    default: 0,
  },
  lastPlayedDate: {
    type: Date,
    default: null,
  },
  createdAt: {
    type: Date,
    default: Date.now,
  },
});

module.exports = mongoose.model("PatientProfile", PatientProfileSchema);