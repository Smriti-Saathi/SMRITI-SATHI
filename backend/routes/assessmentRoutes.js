const express = require("express");
const router = express.Router();
const { createGameSession } = require("../controllers/gameSessionController");

// Safely handle both default export and named export for auth middleware
const authMiddleware = require("../middleware/authMiddleware");
const verifyToken =
  typeof authMiddleware === "function"
    ? authMiddleware
    : authMiddleware.verifyToken;

// When the mobile app POSTs a completed game score here:
router.post("/session", verifyToken, createGameSession);

module.exports = router;
