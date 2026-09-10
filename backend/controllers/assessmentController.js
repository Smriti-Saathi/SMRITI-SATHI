const asyncHandler = require("express-async-handler");
const CognitiveAssessment = require("../models/CognitiveAssessment");
const PatientProfile = require("../models/PatientProfile");

// @desc    Get the latest cognitive assessment for a patient
// @route   GET /api/assessments/patient/:patientId/latest
// @access  Private (Caregiver or Patient)
const getLatestAssessment = asyncHandler(async (req, res) => {
  const { patientId } = req.params;

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
    throw new Error("Forbidden: Unauthorized to view this assessment");
  }

  // Find the single most recent assessment sorted by timestamp descending
  const latestAssessment = await CognitiveAssessment.findOne({
    patientId,
  }).sort({ timestamp: -1 });

  res.status(200).json({
    success: true,
    data: latestAssessment || null,
  });
});

// @desc    Get assessment trend array for the last N days (sorted ascending by timestamp)
// @route   GET /api/assessments/patient/:patientId/trend?days=7
// @access  Private (Caregiver or Patient)
const getAssessmentTrend = asyncHandler(async (req, res) => {
  const { patientId } = req.params;
  const days = parseInt(req.query.days, 10) || 7; // Default to 7 days if not specified

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
    throw new Error("Forbidden: Unauthorized to view assessment trends");
  }

  // Calculate cut-off date based on query parameter
  const cutoffDate = new Date();
  cutoffDate.setDate(cutoffDate.getDate() - days);

  // Fetch assessments within the timeframe, sorted ascending (oldest to newest) for charts
  const assessments = await CognitiveAssessment.find({
    patientId,
    timestamp: { $gte: cutoffDate },
  }).sort({ timestamp: 1 });

  res.status(200).json({
    success: true,
    count: assessments.length,
    data: assessments, // Returns empty array [] safely if no data exists yet
  });
});

module.exports = {
  getLatestAssessment,
  getAssessmentTrend,
};
