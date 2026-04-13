const express = require("express");
const { requestOtp, verifyOtp, completeOnboarding, me } = require("../controllers/authController");
const authenticate = require("../middleware/auth");

const router = express.Router();

router.post("/request-otp", requestOtp);
router.post("/verify-otp", verifyOtp);
router.post("/onboarding", authenticate, completeOnboarding);
router.get("/me", authenticate, me);

module.exports = router;
