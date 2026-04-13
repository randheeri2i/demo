const jwt = require("jsonwebtoken");
const { z } = require("zod");
const { pgPool } = require("../config/db");
const env = require("../config/env");
const { generateOtpCode, hashOtp, compareOtp, normalizePhone } = require("../utils/otp");

const requestOtpSchema = z.object({
  phone: z.string().min(8).max(15),
});

const verifyOtpSchema = z.object({
  phone: z.string().min(8).max(15),
  otp: z.string().length(6),
});

const onboardingSchema = z.object({
  fullName: z.string().min(2),
  email: z.string().email(),
  city: z.string().min(2),
  agencyName: z.string().min(2),
});

const signToken = (broker) =>
  jwt.sign({ brokerId: broker.id, phone: broker.phone }, env.jwtSecret, {
    expiresIn: env.jwtExpiresIn,
  });

const mapBroker = (broker) => ({
  id: broker.id,
  phone: broker.phone,
  fullName: broker.full_name,
  email: broker.email,
  city: broker.city,
  agencyName: broker.agency_name,
  isOnboarded: broker.is_onboarded,
});

const requestOtp = async (req, res, next) => {
  try {
    const { phone } = requestOtpSchema.parse(req.body);
    const normalizedPhone = normalizePhone(phone);
    const otp = generateOtpCode();
    const otpHash = await hashOtp(otp);

    await pgPool.query(
      "INSERT INTO otp_sessions (phone, otp_hash, expires_at, consumed) VALUES ($1, $2, NOW() + INTERVAL '5 minutes', false)",
      [normalizedPhone, otpHash]
    );

    res.status(200).json({
      message: "OTP sent successfully",
      phone: normalizedPhone,
      otp,
    });
  } catch (error) {
    next(error);
  }
};

const verifyOtp = async (req, res, next) => {
  try {
    const { phone, otp } = verifyOtpSchema.parse(req.body);
    const normalizedPhone = normalizePhone(phone);

    const otpSession = await pgPool.query(
      `SELECT id, otp_hash FROM otp_sessions
       WHERE phone = $1 AND consumed = false AND expires_at > NOW()
       ORDER BY created_at DESC LIMIT 1`,
      [normalizedPhone]
    );

    if (!otpSession.rows.length) {
      return res.status(400).json({ message: "OTP expired or not found" });
    }

    const session = otpSession.rows[0];
    const isValidOtp = await compareOtp(otp, session.otp_hash);
    if (!isValidOtp) {
      return res.status(400).json({ message: "Invalid OTP" });
    }

    await pgPool.query("UPDATE otp_sessions SET consumed = true WHERE id = $1", [session.id]);

    const brokerResult = await pgPool.query("SELECT * FROM brokers WHERE phone = $1 LIMIT 1", [normalizedPhone]);

    let broker = brokerResult.rows[0];
    if (!broker) {
      const inserted = await pgPool.query(
        "INSERT INTO brokers (phone, is_onboarded) VALUES ($1, false) RETURNING *",
        [normalizedPhone]
      );
      broker = inserted.rows[0];
    }

    res.status(200).json({
      message: "OTP verified",
      token: signToken(broker),
      broker: mapBroker(broker),
    });
  } catch (error) {
    next(error);
  }
};

const completeOnboarding = async (req, res, next) => {
  try {
    const payload = onboardingSchema.parse(req.body);

    const updated = await pgPool.query(
      `UPDATE brokers
       SET full_name = $1, email = $2, city = $3, agency_name = $4, is_onboarded = true
       WHERE id = $5
       RETURNING *`,
      [payload.fullName, payload.email, payload.city, payload.agencyName, req.user.id]
    );

    res.status(200).json({
      message: "Onboarding completed",
      broker: mapBroker(updated.rows[0]),
    });
  } catch (error) {
    next(error);
  }
};

const me = async (req, res, next) => {
  try {
    const brokerResult = await pgPool.query("SELECT * FROM brokers WHERE id = $1 LIMIT 1", [req.user.id]);

    if (!brokerResult.rows.length) {
      return res.status(404).json({ message: "Broker not found" });
    }

    res.status(200).json({ broker: mapBroker(brokerResult.rows[0]) });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  requestOtp,
  verifyOtp,
  completeOnboarding,
  me,
};
