const mongoose = require("mongoose");

const connectDB = async (retries = 5) => {
  while (retries) {
    try {
      const conn = await mongoose.connect(process.env.MONGO_URI);
      console.log(`✅ MongoDB Connected: ${conn.connection.host}`);
      break;
    } catch (error) {
      console.error(`❌ MongoDB Connection Failed: ${error.message}`);
      retries -= 1;
      console.log(`🔄 Retries left: ${retries}`);

      if (retries === 0) {
        console.error("🚨 No more retries left. Shutting down server.");
        process.exit(1);
      }
      await new Promise((res) => setTimeout(res, 5000));
    }
  }
};

module.exports = connectDB;
