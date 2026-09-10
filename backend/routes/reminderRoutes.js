const express = require("express");
const router = express.Router();
const passport = require("passport");
const roleCheck = require("../middleware/roleCheck");
const {
  createReminder,
  getPatientReminders,
  updateReminderStatus,
  deleteReminder,
} = require("../controllers/reminderController");
const {
  validateCreateReminder,
  validateStatusUpdate,
} = require("../middleware/reminderValidation");

// All reminder routes require authentication and caregiver role
router.use(
  passport.authenticate("jwt", { session: false }),
  roleCheck("caregiver"),
);

router.post("/", validateCreateReminder, createReminder);
router.get("/patient/:patientId", getPatientReminders);
router.patch("/:id/status", validateStatusUpdate, updateReminderStatus);
router.delete("/:id", deleteReminder);

module.exports = router;
