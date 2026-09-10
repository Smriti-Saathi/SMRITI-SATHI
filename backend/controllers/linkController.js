const asyncHandler = require("express-async-handler");
const PatientProfile = require("../models/PatientProfile");
const User = require("../models/User");

// @desc    Link a caregiver to a patient using an invite code
// @route   POST /api/link
// @access  Private (Caregiver only)
const linkPatient = asyncHandler(async (req, res) => {
  const { inviteCode } = req.body;

  if (!inviteCode) {
    res.status(400);
    throw new Error("Please provide an invite code");
  }

  // 1. Find the patient profile matching the invite code
  const profile = await PatientProfile.findOne({ inviteCode }).populate(
    "userId",
    "name age phone",
  );

  if (!profile) {
    res.status(404);
    throw new Error("Invalid or non-existent invite code");
  }

  // 2. Check if already linked to another caregiver
  if (
    profile.caregiverId &&
    profile.caregiverId.toString() !== req.user._id.toString()
  ) {
    res.status(400);
    throw new Error("This patient is already linked to another caregiver");
  }

  // 3. If already linked to THIS caregiver, let them know gracefully
  if (
    profile.caregiverId &&
    profile.caregiverId.toString() === req.user._id.toString()
  ) {
    return res.status(200).json({
      success: true,
      message: "You are already linked to this patient",
      patient: {
        id: profile.userId._id,
        name: profile.userId.name,
        age: profile.userId.age,
      },
    });
  }

  // 4. Link the caregiver ID
  profile.caregiverId = req.user._id;
  await profile.save();

  res.status(200).json({
    success: true,
    message: "Successfully linked to patient",
    patient: {
      id: profile.userId._id,
      name: profile.userId.name,
      age: profile.userId.age,
    },
  });
});

module.exports = { linkPatient };
