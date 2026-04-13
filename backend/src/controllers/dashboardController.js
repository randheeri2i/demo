const Deal = require("../models/Deal");
const Visit = require("../models/Visit");

const getDashboardSummary = async (req, res, next) => {
  try {
    const brokerId = req.user.id;
    const start = new Date();
    start.setHours(0, 0, 0, 0);
    const end = new Date(start);
    end.setDate(end.getDate() + 1);

    const [activeDeals, visitsToday] = await Promise.all([
      Deal.countDocuments({
        brokerId,
        visitStatus: { $in: ["Scheduled", "Visited", "Negotiating"] },
      }),
      Visit.countDocuments({
        brokerId,
        scheduledAt: { $gte: start, $lt: end },
      }),
    ]);

    res.status(200).json({ activeDeals, visitsToday });
  } catch (error) {
    next(error);
  }
};

module.exports = { getDashboardSummary };
