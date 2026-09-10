const asyncHandler = require("express-async-handler");
const MemoryMoment = require("../models/MemoryMoment");
const PatientProfile = require("../models/PatientProfile");
const {
  uploadStreamToCloudinary,
  cloudinary,
} = require("../config/cloudinary");

// @desc    Create a new memory moment with photo upload
// @route   POST /api/memory-moments
// @access  Private (Caregiver only)
const createMemoryMoment = asyncHandler(async (req, res) => {
  const { patientId, caption, year, location, tags } = req.body;

  // 1. Verify file was attached by multer
  if (!req.file) {
    res.status(400);
    throw new Error("An image file is required for a memory moment");
  }

  // 2. Verify patient profile ownership
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
      "Forbidden: Unauthorized to create memory moments for this patient",
    );
  }

  // 3. Upload buffer to Cloudinary via stream
  const cloudinaryResult = await uploadStreamToCloudinary(req.file.buffer);

  // 4. Parse tags safely if sent as string or array
  let parsedTags = [];
  if (tags) {
    parsedTags = typeof tags === "string" ? JSON.parse(tags) : tags;
  }

  // 5. Save Document to MongoDB
  const memoryMoment = await MemoryMoment.create({
    patientId,
    imageUrl: cloudinaryResult.secure_url,
    cloudinaryId: cloudinaryResult.public_id,
    caption: caption || "",
    year: year ? Number(year) : undefined,
    location: location || "",
    tags: parsedTags,
    uploadedBy: req.user._id,
  });

  res.status(201).json({
    success: true,
    data: memoryMoment,
  });
});

// @desc    Get all memory moments for a patient
// @route   GET /api/memory-moments/patient/:patientId
// @access  Private (Caregiver or Patient)
const getPatientMoments = asyncHandler(async (req, res) => {
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
    throw new Error("Forbidden: Unauthorized to view these memory moments");
  }

  const moments = await MemoryMoment.find({ patientId }).sort({
    createdAt: -1,
  });

  res.status(200).json({
    success: true,
    count: moments.length,
    data: moments,
  });
});

// @desc    Delete a memory moment (removes from Cloudinary first, then DB)
// @route   DELETE /api/memory-moments/:id
// @access  Private (Caregiver only)
const deleteMemoryMoment = asyncHandler(async (req, res) => {
  const { id } = req.params;

  const moment = await MemoryMoment.findById(id);
  if (!moment) {
    res.status(404);
    throw new Error("Memory moment not found");
  }

  // Verify ownership via patient profile
  const profile = await PatientProfile.findOne({ userId: moment.patientId });
  if (
    !profile ||
    !profile.caregiverId ||
    profile.caregiverId.toString() !== req.user._id.toString()
  ) {
    res.status(403);
    throw new Error("Forbidden: Unauthorized to delete this memory moment");
  }

  // 1. Delete asset from Cloudinary first (Resilient: log warning if it fails, but don't block DB deletion)
  if (moment.cloudinaryId) {
    try {
      await cloudinary.uploader.destroy(moment.cloudinaryId);
    } catch (cloudError) {
      console.warn(
        `[WARNING] Failed to delete image asset ${moment.cloudinaryId} from Cloudinary:`,
        cloudError.message,
      );
    }
  }

  // 2. Delete database document
  await moment.deleteOne();

  res.status(200).json({
    success: true,
    message: "Memory moment deleted successfully",
  });
});

module.exports = {
  createMemoryMoment,
  getPatientMoments,
  deleteMemoryMoment,
};
