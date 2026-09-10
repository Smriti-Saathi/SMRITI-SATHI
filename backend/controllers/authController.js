const asyncHandler = require("express-async-handler");
const User = require("../models/User");
const PatientProfile = require("../models/PatientProfile");
const generateToken = require("../utils/generateToken");

// @desc    Register a new user
// @route   POST /api/auth/register
// @access  Public
const register = asyncHandler(async (req, res) => {
  const { name, age, role, phone, password, email, preferredLanguage, region } =
    req.body;

  // 1. Validate required fields
  if (!name || !age || !role || !phone || !password) {
    res.status(400);
    throw new Error("Please provide all required fields");
  }

  // 2. Check phone uniqueness
  const userExists = await User.findOne({ phone });
  if (userExists) {
    res.status(400);
    throw new Error("Phone number is already registered");
  }

  // 3. Create User (Password hashing happens automatically in User model pre-validate hook)
  const user = await User.create({
    name,
    age,
    role,
    phone,
    password,
    email,
    preferredLanguage,
    region,
  });

  // 4. Create PatientProfile if role is patient
  if (role === "patient") {
    await PatientProfile.create({ userId: user._id });
  }

  // 5. Clean up response object (strip passwordHash)
  const userObj = user.toObject();
  delete userObj.passwordHash;
  delete userObj._password;

  res.status(201).json({
    success: true,
    token: generateToken(user._id),
    user: userObj,
  });
});

// @desc    Authenticate user & get token
// @route   POST /api/auth/login
// @access  Public
const login = asyncHandler(async (req, res) => {
  const { phone, password } = req.body;

  if (!phone || !password) {
    res.status(400);
    throw new Error("Please provide phone and password");
  }

  const user = await User.findOne({ phone });

  // Generic 401 response to prevent phone number enumeration
  if (!user || !(await user.comparePassword(password))) {
    res.status(401);
    throw new Error("Invalid credentials");
  }

  const userObj = user.toObject();
  delete userObj.passwordHash;

  res.status(200).json({
    success: true,
    token: generateToken(user._id),
    user: userObj,
  });
});

module.exports = { register, login };
