const mongoose = require("mongoose");

const reminderSchema = new mongoose.Schema(
  {
    patientId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: [true, "Patient ID is required"],
      index: true, // Individual index
    },
    type: {
      type: String,
      enum: ["medicine", "hydration", "activity", "appointment"],
      required: [true, "Reminder type is required"],
    },
    title: {
      type: String,
      required: [true, "Title is required"],
    },
    scheduledTime: {
      type: Date,
      required: [true, "Scheduled time is required"],
    },
    recurrence: {
      type: String,
      enum: ["none", "daily", "weekly"],
      default: "none",
    },
    status: {
      type: String,
      enum: ["pending", "done", "missed"],
      default: "pending",
    },
    medicineName: {
      type: String, // Optional: Only required in frontend if type === 'medicine'
    },
    dosage: {
      type: String, // Optional: e.g., '2 pills', '10ml'
    },
    audioCueUrl: {
      type: String, // Added for Smriti Sathi: Stores the S3/Cloudinary link for the Assamese/English voice prompt
    },
  },
  {
    timestamps: true, // Automatically adds createdAt and updatedAt
  },
);

// Compound index for rapid Caregiver Dashboard querying
// (e.g. "Find all pending reminders for Patient X sorted by time")
reminderSchema.index({ patientId: 1, scheduledTime: 1 });

module.exports = mongoose.model("Reminder", reminderSchema);
