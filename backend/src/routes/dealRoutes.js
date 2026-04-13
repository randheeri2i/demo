const express = require("express");
const { getMyDeals, createDeal, updateDealStatus } = require("../controllers/dealController");
const authenticate = require("../middleware/auth");

const router = express.Router();

router.get("/my-deals", authenticate, getMyDeals);
router.post("/", authenticate, createDeal);
router.patch("/:dealId/status", authenticate, updateDealStatus);

module.exports = router;
