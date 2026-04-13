const cors = require("cors");
const express = require("express");
const env = require("./config/env");
const authRoutes = require("./routes/authRoutes");
const propertyRoutes = require("./routes/propertyRoutes");
const dealRoutes = require("./routes/dealRoutes");
const dashboardRoutes = require("./routes/dashboardRoutes");
const visitRoutes = require("./routes/visitRoutes");

const app = express();

app.use(
  cors({
    origin: env.clientUrl,
  })
);
app.use(express.json({ limit: "10mb" }));

app.get("/api/health", (_req, res) => {
  res.json({ ok: true, service: "PinMyHome API" });
});

app.use("/api/auth", authRoutes);
app.use("/api/properties", propertyRoutes);
app.use("/api/deals", dealRoutes);
app.use("/api/dashboard", dashboardRoutes);
app.use("/api/visits", visitRoutes);

app.use((err, _req, res, _next) => {
  console.error(err);
  res.status(500).json({ message: err.message || "Internal server error" });
});

module.exports = app;
