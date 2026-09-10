const asyncHandler = require("express-async-handler");
const Reminder = require("../models/Reminder");
const PatientProfile = require("../models/PatientProfile");

// @desc    Create a new reminder
// @route   POST /api/reminders
// @access  Private (Caregiver only)
const createReminder = asyncHandler(async (req, res) => {
  const {
    patientId,
    type,
    title,
    scheduledTime,
    description,
    medicineName,
    dosage,
  } = req.body;

  // Verify patient profile exists and caregiver is authorized
  const profile = await PatientProfile.findOne({ userId: patientId });
  if (!profile) {
    res.status(404);
    throw new Error("Patient profile not found");
  }

  if (
    !profile.caregiverId ||
    profile.caregiverId.toString() !== req.user._id.toString()
  ) {
    res.status(403);
    throw new Error(
      "Forbidden: You are not authorized to create reminders for this patient",
    );
  }

  const reminder = await Reminder.create({
    patientId,
    type,
    title,
    scheduledTime,
    description,
    medicineName,
    dosage,
    status: "pending",
  });

  res.status(201).json({
    success: true,
    data: reminder,
  });
});

// @desc    Get all reminders for a patient (sorted by scheduledTime ASC, filterable by ?status=)
// @route   GET /api/reminders/patient/:patientId
// @access  Private (Caregiver only)
const getPatientReminders = asyncHandler(async (req, res) => {
  const { patientId } = req.params;
  const { status } = req.query;

  // Verify caregiver authorization
  const profile = await PatientProfile.findOne({ userId: patientId });
  if (!profile) {
    res.status(404);
    throw new Error("Patient profile not found");
  }

  if (
    !profile.caregiverId ||
    profile.caregiverId.toString() !== req.user._id.toString()
  ) {
    res.status(403);
    throw new Error(
      "Forbidden: You are not authorized to view this patient records",
    );
  }

  // Build filter query
  const query = { patientId };
  if (status) {
    const validStatuses = ["pending", "done", "missed", "snoozed"];
    if (!validStatuses.includes(status)) {
      res.status(400);
      throw new Error("Invalid status filter value");
    }
    query.status = status;
  }

  // Fetch and sort ascending by scheduledTime
  const reminders = await Reminder.find(query).sort({ scheduledTime: 1 });

  res.status(200).json({
    success: true,
    count: reminders.length,
    data: reminders,
  });
});

// @desc    Update reminder status only
// @route   PATCH /api/reminders/:id/status
// @access  Private (Caregiver only)
const updateReminderStatus = asyncHandler(async (req, res) => {
  const { id } = req.params;
  const { status } = req.body;

  const reminder = await Reminder.findById(id);
  if (!reminder) {
    res.status(404);
    throw new Error("Reminder not found");
  }

  // Check if caregiver owns the patient profile tied to this reminder
  const profile = await PatientProfile.findOne({ userId: reminder.patientId });
  if (
    !profile ||
    !profile.caregiverId ||
    profile.caregiverId.toString() !== req.user._id.toString()
  ) {
    res.status(403);
    throw new Error("Forbidden: Unauthorized to update this reminder");
  }

  reminder.status = status;
  await reminder.save();

  res.status(200).json({
    success: true,
    data: reminder,
  });
});

// @desc    Delete a reminder
// @route   POST /api/reminders/:id (or DELETE)
// @access  Private (Caregiver only)
const deleteReminder = asyncHandler(async (req, res) => {
  const { id } = req.params;

  const reminder = await Reminder.findById(id);
  if (!reminder) {
    res.status(404);
    throw new Error("Reminder not found");
  }

  const profile = await PatientProfile.findOne({ userId: reminder.patientId });
  if (
    !profile ||
    !profile.caregiverId ||
    profile.caregiverId.toString() !== req.user._id.toString()
  ) {
    res.status(403);
    throw new Error("Forbidden: Unauthorized to delete this reminder");
  }

  await reminder.deleteOne();

  res.status(200).json({
    success: true,
    message: "Reminder deleted successfully",
  });
});

module.exports = {
  createReminder,
  getPatientReminders,
  updateReminderStatus,
  deleteReminder,
};
