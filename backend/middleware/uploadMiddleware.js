const multer = require("multer");

// Store file temporarily in memory buffer
const storage = multer.memoryStorage();

const fileFilter = (req, file, cb) => {
  // Allow only image mimetypes
  if (file.mimetype.startsWith("image/")) {
    cb(null, true);
  } else {
    cb(
      new Error("Only image files (JPEG, PNG, WebP, etc.) are allowed!"),
      false,
    );
  }
};

const upload = multer({
  storage,
  limits: {
    fileSize: 5 * 1024 * 1024, // 5MB limit
  },
  fileFilter,
});

module.exports = upload;
