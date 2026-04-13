const { z } = require("zod");
const Property = require("../models/Property");

const createPropertySchema = z.object({
  title: z.string().min(2),
  bhk: z.coerce.number().min(1),
  areaSqft: z.coerce.number().min(1),
  expectedPrice: z.coerce.number().min(1),
  address: z.string().min(5),
  latitude: z.coerce.number().optional(),
  longitude: z.coerce.number().optional(),
  areaTag: z.string().min(2).default("Noida"),
  photos: z.array(z.string()).default([]),
  platformMargin: z.coerce.number().min(0).default(0),
  isHot: z.boolean().default(true),
});

const createProperty = async (req, res, next) => {
  try {
    const payload = createPropertySchema.parse(req.body);

    const property = await Property.create({
      brokerId: req.user.id,
      title: payload.title,
      bhk: payload.bhk,
      areaSqft: payload.areaSqft,
      expectedPrice: payload.expectedPrice,
      address: payload.address,
      location: {
        lat: payload.latitude,
        lng: payload.longitude,
      },
      areaTag: payload.areaTag,
      photos: payload.photos,
      platformMargin: payload.platformMargin,
      isHot: payload.isHot,
    });

    res.status(201).json({ message: "Property added", property });
  } catch (error) {
    next(error);
  }
};

const getMyProperties = async (req, res, next) => {
  try {
    const listings = await Property.find({ brokerId: req.user.id }).sort({ createdAt: -1 }).lean();
    res.status(200).json({ listings });
  } catch (error) {
    next(error);
  }
};

const getHotListings = async (req, res, next) => {
  try {
    const { search = "", area = "", minPrice, maxPrice, bhk } = req.query;
    const filters = { isHot: true };

    if (search) filters.title = { $regex: search, $options: "i" };
    if (area) filters.areaTag = { $regex: area, $options: "i" };
    if (bhk) filters.bhk = Number(bhk);

    if (minPrice || maxPrice) {
      filters.expectedPrice = {};
      if (minPrice) filters.expectedPrice.$gte = Number(minPrice);
      if (maxPrice) filters.expectedPrice.$lte = Number(maxPrice);
    }

    const listings = await Property.find(filters).sort({ createdAt: -1 }).limit(40).lean();
    res.status(200).json({ listings });
  } catch (error) {
    next(error);
  }
};

const getPropertyById = async (req, res, next) => {
  try {
    const property = await Property.findById(req.params.id).lean();
    if (!property) {
      return res.status(404).json({ message: "Property not found" });
    }

    res.status(200).json({ property });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  createProperty,
  getMyProperties,
  getHotListings,
  getPropertyById,
};
