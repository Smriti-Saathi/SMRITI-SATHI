const express = require("express");
const router = express.Router();
const passport = require("passport");
const {
  createGameSession,
  getPatientSessions,
  getPatientStats,
} = require("../controllers/sessionController");
const { validateCreateSession } = require("../middleware/sessionValidation");

// All session routes require authentication
router.use(passport.authenticate("jwt", { session: false }));

router.post("/", validateCreateSession, createGameSession);
router.get("/patient/:patientId", getPatientSessions);
router.get("/patient/:patientId/stats", getPatientStats);

module.exports = router;
