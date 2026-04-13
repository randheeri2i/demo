const mongoose = require("mongoose");

const dealSchema = new mongoose.Schema(
  {
    brokerId: { type: Number, required: true, index: true },
    propertyId: { type: mongoose.Schema.Types.ObjectId, ref: "Property", required: true },
    propertyTitle: { type: String, required: true, trim: true },
    buyerName: { type: String, required: true, trim: true },
    visitStatus: {
      type: String,
      enum: ["Scheduled", "Visited", "Negotiating", "Closed"],
      default: "Scheduled",
    },
    dealValue: { type: Number, default: 0 },
  },
  { timestamps: true },
);

module.exports = mongoose.model("Deal", dealSchema);
