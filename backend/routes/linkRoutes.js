const express = require("express");
const router = express.Router();
const passport = require("passport");
const roleCheck = require("../middleware/roleCheck");
const { linkPatient } = require("../controllers/linkController");

// Secure route: Must be logged in AND have the 'caregiver' role
router.post(
  "/",
  passport.authenticate("jwt", { session: false }),
  roleCheck("caregiver"),
  linkPatient,
);

module.exports = router;
