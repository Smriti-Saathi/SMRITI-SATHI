const cloudinary = require("cloudinary").v2;
const streamifier = require("streamifier");

// Configure Cloudinary using environment variables
cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
  api_key: process.env.CLOUDINARY_API_KEY,
  api_secret: process.env.CLOUDINARY_API_SECRET,
});

/**
 * Uploads an image buffer directly to Cloudinary using upload_stream
 * @param {Buffer} fileBuffer - The raw file buffer from Multer
 * @returns {Promise<Object>} - Resolves with Cloudinary upload result
 */
const uploadStreamToCloudinary = (fileBuffer) => {
  return new Promise((resolve, reject) => {
    const uploadStream = cloudinary.uploader.upload_stream(
      {
        folder: "smriti_sathi/memory_moments",
        resource_type: "image",
      },
      (error, result) => {
        if (error) return reject(error);
        resolve(result);
      },
    );

    streamifier.createReadStream(fileBuffer).pipe(uploadStream);
  });
};

module.exports = { cloudinary, uploadStreamToCloudinary };
