const { body, validationResult } = require("express-validator");

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

const validateCreateSession = [
  body("patientId")
    .notEmpty()
    .withMessage("Patient ID is required")
    .isMongoId()
    .withMessage("Invalid Patient ID format"),
  body("gameType")
    .notEmpty()
    .withMessage("Game type is required")
    .isIn(["objectRecall", "faceMatching", "pathFinding", "rhythmSequence"])
    .withMessage("Invalid game type value"),
  body("difficulty")
    .notEmpty()
    .withMessage("Difficulty is required")
    .isIn(["easy", "moderate", "hard"])
    .withMessage("Invalid difficulty setting"),
  body("score")
    .notEmpty()
    .withMessage("Score is required")
    .isInt({ min: 0 })
    .withMessage("Score must be a positive integer"),
  body("accuracy")
    .notEmpty()
    .withMessage("Accuracy is required")
    .isFloat({ min: 0, max: 100 })
    .withMessage("Accuracy must be a percentage between 0 and 100"),
  body("reactionTimeMs")
    .notEmpty()
    .withMessage("Reaction time is required")
    .isInt({ min: 100, max: 60000 })
    .withMessage("Reaction time must be between 100ms and 60,000ms"),
  body("durationSeconds")
    .notEmpty()
    .withMessage("Duration is required")
    .isInt({ min: 1, max: 3600 })
    .withMessage("Duration must be between 1 and 3600 seconds"),
  validate,
];

module.exports = {
  validateCreateSession,
};
