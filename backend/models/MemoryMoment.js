const mongoose = require("mongoose");

const memoryMomentSchema = new mongoose.Schema(
  {
    patientId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: [true, "Patient ID is required"],
      index: true, // Ensures fast retrieval of a patient's photo gallery
    },
    imageUrl: {
      type: String,
      required: [true, "Image URL is required"], // Will store the Cloudinary secure_url
    },
    cloudinaryId: {
      type: String,
      required: [true, "Cloudinary ID is required for cleanup operations"], // Stored for file deletion
    },
    personName: {
      type: String,
      required: [true, "Person's name is required"],
    },
    relationship: {
      type: String,
      required: [true, "Relationship is required (e.g., Daughter, Grandson)"],
    },
    description: {
      type: String,
      maxlength: [300, "Description cannot exceed 300 characters"],
    },
    createdBy: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: [true, "Creator ID is required"],
    },
  },
  {
    timestamps: true, // Automatically adds createdAt and updatedAt
  },
);

module.exports = mongoose.model("MemoryMoment", memoryMomentSchema);
