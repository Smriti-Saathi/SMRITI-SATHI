const mongoose = require("mongoose");
const bcrypt = require("bcryptjs");

const userSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, "Name is required"],
  },
  age: {
    type: Number,
    required: [true, "Age is required"],
    min: [1, "Age cannot be less than 1"],
    max: [120, "Age cannot exceed 120"],
  },
  role: {
    type: String,
    enum: ["patient", "caregiver", "healthcareWorker"],
    required: [true, "Role is required"],
  },
  phone: {
    type: String,
    required: [true, "Phone number is required"],
    unique: true,
  },
  email: {
    type: String,
    unique: true,
    sparse: true,
  },
  passwordHash: {
    type: String,
    required: [true, "Password is required"],
  },
  preferredLanguage: {
    type: String,
    enum: ["english", "assamese"],
    default: "english",
  },
  region: {
    type: String,
    default: "NER",
  },
  createdAt: {
    type: Date,
    default: Date.now,
  },
});

userSchema.virtual("password").set(function (value) {
  this._password = value;
});

// HASH DURING VALIDATION PHASE
userSchema.pre("validate", async function () {
  if (this._password) {
    const salt = await bcrypt.genSalt(10);
    this.passwordHash = await bcrypt.hash(this._password, salt);
    this._password = undefined;
  }
});

userSchema.methods.comparePassword = async function (candidatePassword) {
  return await bcrypt.compare(candidatePassword, this.passwordHash);
};

module.exports = mongoose.model("User", userSchema);
