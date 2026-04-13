const express = require("express");
const {
  createProperty,
  getMyProperties,
  getHotListings,
  getPropertyById,
} = require("../controllers/propertyController");
const authenticate = require("../middleware/auth");

const router = express.Router();

router.post("/", authenticate, createProperty);
router.get("/mine", authenticate, getMyProperties);
router.get("/hot", authenticate, getHotListings);
router.get("/:id", authenticate, getPropertyById);

module.exports = router;
