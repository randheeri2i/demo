const app = require("./app");
const { initPostgres, connectMongo } = require("./config/db");
const env = require("./config/env");

const start = async () => {
  try {
    await initPostgres();
    await connectMongo();

    app.listen(env.port, () => {
      console.log(`PinMyHome API running on port ${env.port}`);
    });
  } catch (error) {
    console.error("Failed to start server", error);
    process.exit(1);
  }
};

start();
