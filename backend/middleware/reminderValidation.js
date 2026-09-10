const { body, validationResult } = require("express-validator");

// Helper middleware to catch and return 400 with structured field errors
const validate = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      success: false,
      errors: errors.array().map((err) => ({
        field: err.path,
        message: err.msg,
      })),
    });
  }
  next();
};

const validateCreateReminder = [
  body("patientId")
    .notEmpty()
    .withMessage("Patient ID is required")
    .isMongoId()
    .withMessage("Invalid Patient ID format"),
  body("type")
    .notEmpty()
    .withMessage("Reminder type is required")
    .isIn(["medicine", "appointment", "hydration", "activity", "custom"])
    .withMessage("Invalid reminder type value"),
  body("title")
    .notEmpty()
    .withMessage("Title is required")
    .isString()
    .withMessage("Title must be text"),
  body("scheduledTime")
    .notEmpty()
    .withMessage("Scheduled time is required")
    .isISO8601()
    .withMessage("Scheduled time must be a valid ISO 8601 date string")
    .custom((value) => {
      const inputDate = new Date(value);
      const currentDate = new Date();
      // Reset hours of current date to start of day if you want to allow full-day 'today' matching,
      // or compare exact timestamps. Here we allow any time today or in the future:
      currentDate.setHours(0, 0, 0, 0);

      if (inputDate < currentDate) {
        throw new Error("Scheduled time must be today or a future date");
      }
      return true;
    }),
  validate,
];

const validateStatusUpdate = [
  body("status")
    .notEmpty()
    .withMessage("Status is required")
    .isIn(["pending", "done", "missed", "snoozed"])
    .withMessage(
      "Invalid status value. Allowed: pending, done, missed, snoozed",
    ),
  validate,
];

module.exports = {
  validateCreateReminder,
  validateStatusUpdate,
};
