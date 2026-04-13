const mongoose = require("mongoose");

const propertySchema = new mongoose.Schema(
  {
    brokerId: { type: Number, required: true, index: true },
    title: { type: String, required: true, trim: true },
    bhk: { type: Number, required: true, min: 1 },
    areaSqft: { type: Number, required: true, min: 1 },
    expectedPrice: { type: Number, required: true, min: 0 },
    platformMargin: { type: Number, default: 0, min: 0 },
    address: { type: String, required: true, trim: true },
    location: {
      lat: Number,
      lng: Number,
    },
    areaTag: { type: String, default: "Noida", trim: true },
    photos: { type: [String], default: [] },
    isHot: { type: Boolean, default: true },
  },
  { timestamps: true }
);

module.exports = mongoose.model("Property", propertySchema);
