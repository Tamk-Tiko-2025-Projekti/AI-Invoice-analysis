/* global process */
const express = require('express');
const app = express();
const port = process.env.PORT | 3000;

app.use(express.json());

app.get('/', (req, res) => {
  res.send('Hello World!');
});

function startServer() {
  return new Promise((resolve, reject) => {
    const server = app.listen(port, (err) => {
      if (err) {
        reject('Error starting server: ', err);
      }
      console.log('Server started on port: ', port);
      resolve(server);
    });
  });
}

function closeServer(server) {
  return new Promise((resolve, reject) => {
    server.close((err) => {
      if (err) {
        reject('Error closing server: ', err);
      }
      console.log('Server closed');
      resolve();
    });
  });
}

module.exports = { startServer, closeServer };