const dotenv = require("dotenv");

dotenv.config();

module.exports = {
  port: Number(process.env.PORT || 5000),
  clientUrl: process.env.CLIENT_URL || "http://localhost:5173",
  jwtSecret: process.env.JWT_SECRET || "pinmyhome-dev-secret",
  jwtExpiresIn: process.env.JWT_EXPIRES_IN || "7d",
  postgres: {
    host: process.env.POSTGRES_HOST || "localhost",
    port: Number(process.env.POSTGRES_PORT || 5432),
    user: process.env.POSTGRES_USER || "postgres",
    password: process.env.POSTGRES_PASSWORD || "postgres",
    database: process.env.POSTGRES_DB || "pinmyhome",
  },
  mongoUri: process.env.MONGODB_URI || "mongodb://localhost:27017/pinmyhome",
};
