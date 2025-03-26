/* global process */
const { startServer, closeServer } = require('./server/server');

async function setupServer() {
  try {
    const server = await startServer();
    setupShutdown(server);

  } catch (err) {
    console.error('Error starting server', err);
    process.exit(1);
  }
}

function setupShutdown(server) {
  const shutdown = async () => {
    console.info('Shutting down server');
    try {
      await closeServer(server);

    } catch (err) {
      console.error('Error during shutdown', err);
    }
  };
  process.on('SIGINT', shutdown);
  process.on('SIGTERM', shutdown);
}

setupServer();