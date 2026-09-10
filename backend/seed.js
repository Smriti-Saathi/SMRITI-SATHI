const mongoose = require("mongoose");
const dotenv = require("dotenv");

// Load environment variables
dotenv.config();

// Import Models
const User = require("./models/User");
const PatientProfile = require("./models/PatientProfile");
const GameSession = require("./models/GameSession");
const Reminder = require("./models/Reminder");

// DYNAMIC SCHEMA INJECTION:
// We inject isSeed here so we don't have to pollute the production model files.
User.schema.add({ isSeed: { type: Boolean, default: false } });
PatientProfile.schema.add({ isSeed: { type: Boolean, default: false } });
GameSession.schema.add({ isSeed: { type: Boolean, default: false } });
Reminder.schema.add({ isSeed: { type: Boolean, default: false } });

const seedDatabase = async () => {
  try {
    console.log("Connecting to MongoDB...");
    await mongoose.connect(process.env.MONGO_URI);
    console.log("Connected.");

    console.log("Clearing old seed data (preserving real data)...");
    await User.deleteMany({ isSeed: true });
    await PatientProfile.deleteMany({ isSeed: true });
    await GameSession.deleteMany({ isSeed: true });
    await Reminder.deleteMany({ isSeed: true });

    console.log("Generating Caregiver...");
    const caregiver = await User.create({
      name: "Priya Sharma (Caregiver)",
      age: 45,
      role: "caregiver",
      phone: "+919999999999",
      password: "password123", // Will be hashed by pre-save hook
      preferredLanguage: "english",
      region: "NER",
      isSeed: true,
    });

    console.log("Generating Patients...");
    const patient1 = await User.create({
      name: "Aaba (Grandfather)",
      age: 78,
      role: "patient",
      phone: "+918888888881",
      password: "password123",
      preferredLanguage: "assamese",
      region: "NER",
      isSeed: true,
    });

    const patient2 = await User.create({
      name: "Aita (Grandmother)",
      age: 74,
      role: "patient",
      phone: "+918888888882",
      password: "password123",
      preferredLanguage: "assamese",
      region: "NER",
      isSeed: true,
    });

    // Create Profiles & Link to Caregiver
    await PatientProfile.create([
      {
        userId: patient1._id,
        caregiverId: caregiver._id,
        inviteCode: "SEED01",
        isSeed: true,
      },
      {
        userId: patient2._id,
        caregiverId: caregiver._id,
        inviteCode: "SEED02",
        isSeed: true,
      },
    ]);

    console.log(
      "Generating 14 days of realistic GameSessions for Patient 1...",
    );
    const gameSessions = [];
    const now = new Date();

    // Loop backwards for 14 days
    for (let day = 0; day < 14; day++) {
      const sessionsPerDay = Math.floor(Math.random() * 2) + 2; // 2 or 3 sessions

      for (let session = 0; session < sessionsPerDay; session++) {
        // Base accuracy improves as 'day' gets closer to 0 (today)
        // Day 13 (oldest) = ~50% accuracy. Day 0 (today) = ~75% accuracy
        const learningCurve = 75 - day * 1.8;
        const randomVariation = Math.random() * 10 - 5; // +/- 5%
        const finalAccuracy = Math.min(
          100,
          Math.max(0, Math.round(learningCurve + randomVariation)),
        );

        const timestamp = new Date(now.getTime() - day * 24 * 60 * 60 * 1000);
        timestamp.setHours(10 + session * 4); // Spread throughout the day (10am, 2pm, 6pm)

        gameSessions.push({
          patientId: patient1._id,
          gameType: "objectRecall",
          difficulty: "moderate",
          score: finalAccuracy, // matching score to accuracy for simplicity
          accuracy: finalAccuracy,
          reactionTimeMs: 4000 - finalAccuracy * 10, // Gets faster as accuracy improves
          durationSeconds: 120,
          timestamp: timestamp,
          isSeed: true,
        });
      }
    }
    await GameSession.insertMany(gameSessions);

    console.log("Generating Reminders...");
    await Reminder.create([
      {
        patientId: patient1._id,
        type: "medicine",
        title: "Morning Blood Pressure Meds",
        scheduledTime: new Date(now.getTime() - 2 * 60 * 60 * 1000),
        status: "done",
        medicineName: "Amlodipine",
        dosage: "5mg",
        isSeed: true,
      },
      {
        patientId: patient1._id,
        type: "hydration",
        title: "Drink Water",
        scheduledTime: new Date(now.getTime() - 30 * 60 * 1000),
        status: "missed",
        isSeed: true,
      },
      {
        patientId: patient1._id,
        type: "activity",
        title: "Evening Walk",
        scheduledTime: new Date(now.getTime() + 4 * 60 * 60 * 1000),
        status: "pending",
        isSeed: true,
      },
      {
        patientId: patient2._id,
        type: "appointment",
        title: "Neurologist Visit",
        scheduledTime: new Date(now.getTime() + 24 * 60 * 60 * 1000),
        status: "pending",
        isSeed: true,
      },
    ]);

    console.log("-------------------------------------------");
    console.log("🌱 SEEDING COMPLETE!");
    console.log(`👤 Users Created: 3 (1 Caregiver, 2 Patients)`);
    console.log(`📝 Profiles Linked: 2`);
    console.log(
      `🎮 Game Sessions Created: ${gameSessions.length} (over 14 days)`,
    );
    console.log(`⏰ Reminders Created: 4`);
    console.log("-------------------------------------------");

    process.exit();
  } catch (error) {
    console.error("Error seeding database:", error);
    process.exit(1);
  }
};

seedDatabase();
