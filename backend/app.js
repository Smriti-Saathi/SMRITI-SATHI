const express = require("express");
const cors = require("cors");
const passport = require("passport");

// 1. Initialize express app FIRST
const app = express();

// 2. Middleware configuration
app.use(cors());
app.use(express.json());

// 3. Initialize passport AFTER app is declared
app.use(passport.initialize());
require("./config/passport")(passport);

// 4. Import routes
const authRoutes = require("./routes/authRoutes");
const assessmentRoutes = require("./routes/assessmentRoutes");
const reminderRoutes = require("./routes/reminderRoutes");
const sessionRoutes = require("./routes/sessionRoutes");
const momentRoutes = require("./routes/momentRoutes");
const linkRoutes = require("./routes/linkRoutes");

// 5. Mount routes
app.use("/api/auth", authRoutes);
app.use("/api/assessments", assessmentRoutes);
app.use("/api/reminders", reminderRoutes);
app.use("/api/sessions", sessionRoutes);
app.use("/api/moments", momentRoutes);
app.use("/api/link", linkRoutes);

// Root test endpoint
app.get("/", (req, res) => {
  res.send({ message: "Smriti Sathi Backend API is running successfully!" });
});

module.exports = app;
