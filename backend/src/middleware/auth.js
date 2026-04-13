const jwt = require("jsonwebtoken");
const { pgPool } = require("../config/db");
const env = require("../config/env");

const authenticate = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization || "";
    const token = authHeader.startsWith("Bearer ") ? authHeader.slice(7) : null;

    if (!token) {
      return res.status(401).json({ message: "Authentication token missing" });
    }

    const decoded = jwt.verify(token, env.jwtSecret);
    const result = await pgPool.query("SELECT * FROM brokers WHERE id = $1 LIMIT 1", [decoded.brokerId]);

    if (!result.rows.length) {
      return res.status(401).json({ message: "Broker not found" });
    }

    const broker = result.rows[0];
    req.user = {
      id: broker.id,
      phone: broker.phone,
      fullName: broker.full_name,
      email: broker.email,
      city: broker.city,
      agencyName: broker.agency_name,
      isOnboarded: broker.is_onboarded,
    };

    next();
  } catch (_error) {
    return res.status(401).json({ message: "Invalid or expired token" });
  }
};

module.exports = authenticate;
