const asyncHandler = require("express-async-handler");
const PatientProfile = require("../models/PatientProfile");

const verifyCaregiverAccess = asyncHandler(async (req, res, next) => {
  // Extract patientId from either route params (e.g., /api/patients/:patientId/stats) or request body
  const patientId = req.params.patientId || req.body.patientId;

  if (!patientId) {
    res.status(400);
    throw new Error("Patient ID is required for access verification");
  }

  // Find the patient's profile
  const profile = await PatientProfile.findOne({ userId: patientId });

  if (!profile) {
    res.status(404);
    throw new Error("Patient profile not found");
  }

  // Check if the current user (caregiver) is explicitly linked to this patient
  if (
    !profile.caregiverId ||
    profile.caregiverId.toString() !== req.user._id.toString()
  ) {
    res.status(403);
    throw new Error(
      "Forbidden: You are not authorized to access this patient data",
    );
  }

  // Attach profile to request if controllers need it later
  req.patientProfile = profile;
  next();
});

module.exports = verifyCaregiverAccess;
