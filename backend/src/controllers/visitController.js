const { z } = require("zod");
const Visit = require("../models/Visit");
const Property = require("../models/Property");

const scheduleVisitSchema = z.object({
  propertyId: z.string().min(1),
  buyerName: z.string().min(2),
  buyerPhone: z.string().min(8).max(20),
  scheduledAt: z.string().datetime(),
  notes: z.string().max(500).optional(),
});

const scheduleVisit = async (req, res, next) => {
  try {
    const payload = scheduleVisitSchema.parse(req.body);

    const property = await Property.findById(payload.propertyId);
    if (!property) {
      return res.status(404).json({ message: "Property not found" });
    }

    const visit = await Visit.create({
      property: property._id,
      brokerId: req.user.id,
      buyerName: payload.buyerName,
      buyerPhone: payload.buyerPhone,
      scheduledAt: new Date(payload.scheduledAt),
      notes: payload.notes || "",
      status: "Scheduled",
    });

    res.status(201).json({ message: "Visit scheduled", visit });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  scheduleVisit,
};
