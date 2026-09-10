const express = require("express");
const router = express.Router();
const passport = require("passport");
const roleCheck = require("../middleware/roleCheck");
const upload = require("../middleware/uploadMiddleware");
const {
  createMemoryMoment,
  getPatientMoments,
  deleteMemoryMoment,
} = require("../controllers/momentController");

// All routes require authentication
router.use(passport.authenticate("jwt", { session: false }));

// POST: Caregiver only, expects multipart/form-data with field name 'image'
router.post(
  "/",
  roleCheck("caregiver"),
  upload.single("image"),
  createMemoryMoment,
);

// GET: Accessible by Patient or Caregiver
router.get("/patient/:patientId", getPatientMoments);

// DELETE: Caregiver only
router.delete("/:id", roleCheck("caregiver"), deleteMemoryMoment);

module.exports = router;
