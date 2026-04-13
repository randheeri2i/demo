const bcrypt = require("bcryptjs");

const generateOtpCode = () => String(Math.floor(100000 + Math.random() * 900000));

const normalizePhone = (phone) => String(phone).replace(/[^\d+]/g, "");

const hashOtp = async (otpCode) => bcrypt.hash(otpCode, 10);

const compareOtp = async (otpCode, otpHash) => bcrypt.compare(otpCode, otpHash);

module.exports = {
  generateOtpCode,
  normalizePhone,
  hashOtp,
  compareOtp,
};
