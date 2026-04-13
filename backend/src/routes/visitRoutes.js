const express = require("express");
const { scheduleVisit } = require("../controllers/visitController");
const authenticate = require("../middleware/auth");

const router = express.Router();

router.post("/", authenticate, scheduleVisit);

module.exports = router;
