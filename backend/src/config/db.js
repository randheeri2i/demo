const { Pool } = require("pg");
const mongoose = require("mongoose");
const env = require("./env");

const pgPool = new Pool({
  host: env.postgres.host,
  port: env.postgres.port,
  user: env.postgres.user,
  password: env.postgres.password,
  database: env.postgres.database,
});

const connectMongo = async () => {
  await mongoose.connect(env.mongoUri, {
    serverSelectionTimeoutMS: 10000,
  });
  console.log("MongoDB connected");
};

const initPostgres = async () => {
  await pgPool.query(`
    CREATE TABLE IF NOT EXISTS brokers (
      id SERIAL PRIMARY KEY,
      phone VARCHAR(15) UNIQUE NOT NULL,
      full_name VARCHAR(120),
      email VARCHAR(120),
      city VARCHAR(120),
      agency_name VARCHAR(120),
      is_onboarded BOOLEAN DEFAULT FALSE,
      created_at TIMESTAMP DEFAULT NOW()
    );
  `);

  await pgPool.query(`
    ALTER TABLE brokers
    ADD COLUMN IF NOT EXISTS is_onboarded BOOLEAN DEFAULT FALSE;
  `);

  await pgPool.query(`
    CREATE TABLE IF NOT EXISTS otp_sessions (
      id SERIAL PRIMARY KEY,
      phone VARCHAR(15) NOT NULL,
      otp_hash VARCHAR(255) NOT NULL,
      expires_at TIMESTAMP NOT NULL,
      consumed BOOLEAN DEFAULT FALSE,
      created_at TIMESTAMP DEFAULT NOW()
    );
  `);

  console.log("PostgreSQL ready");
};

module.exports = {
  pgPool,
  connectMongo,
  initPostgres,
};
