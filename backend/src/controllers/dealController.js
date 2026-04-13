const { z } = require("zod");
const Deal = require("../models/Deal");
const Property = require("../models/Property");

const createDealSchema = z.object({
  propertyId: z.string().min(1),
  buyerName: z.string().min(2),
  visitStatus: z.enum(["Scheduled", "Visited", "Negotiating", "Closed"]).optional(),
  dealValue: z.coerce.number().min(0).optional(),
});

const updateDealStatusSchema = z.object({
  visitStatus: z.enum(["Scheduled", "Visited", "Negotiating", "Closed"]),
});

const getMyDeals = async (req, res, next) => {
  try {
    const deals = await Deal.find({ brokerId: req.user.id }).sort({ createdAt: -1 }).lean();
    res.status(200).json({ deals });
  } catch (error) {
    next(error);
  }
};

const createDeal = async (req, res, next) => {
  try {
    const payload = createDealSchema.parse(req.body);
    const property = await Property.findById(payload.propertyId);

    if (!property) {
      return res.status(404).json({ message: "Property not found" });
    }

    const deal = await Deal.create({
      brokerId: req.user.id,
      propertyId: property._id,
      propertyTitle: property.title,
      buyerName: payload.buyerName,
      visitStatus: payload.visitStatus || "Scheduled",
      dealValue: payload.dealValue || 0,
    });

    res.status(201).json({ message: "Deal created", deal });
  } catch (error) {
    next(error);
  }
};

const updateDealStatus = async (req, res, next) => {
  try {
    const { visitStatus } = updateDealStatusSchema.parse(req.body);

    const deal = await Deal.findOneAndUpdate(
      { _id: req.params.dealId, brokerId: req.user.id },
      { visitStatus },
      { new: true }
    );

    if (!deal) {
      return res.status(404).json({ message: "Deal not found" });
    }

    res.status(200).json({ message: "Deal status updated", deal });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  getMyDeals,
  createDeal,
  updateDealStatus,
};
