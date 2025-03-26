/* global process */
const express = require('express');
const multer = require('multer');
const path = require('path');
const { spawn } = require('child_process');
const fs = require('fs');
const cors = require('cors');

const app = express();
const port = process.env.PORT || 3000;

app.use(cors());

// Ensures temp directory exists
const uploadDir = path.join(__dirname, 'temp');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

// Configure multer for file uploads
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    cb(null, file.originalname);
  }
});

const upload = multer({ storage: storage });

app.use(express.json());

app.post('/', upload.single('image'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: 'No image uploaded.' });
    }

    const imagePath = path.resolve(req.file.path);
    console.log(`✅ Image saved at: ${imagePath}`);

    console.log("🚀 Starting Python script...");
    
    // Starts the python process
    const pythonProcess = spawn('python3', ['testi.py', imagePath]);

    let result = '';
    let error = '';

    // Capture output
    pythonProcess.stdout.on('data', (data) => {
      result += data.toString();
    });

    // Capture error
    pythonProcess.stderr.on('data', (data) => {
      error += data.toString();
    });

    // Handle process exit
    pythonProcess.on('close', (code) => {
      if (code !== 0) {
        console.error(`❌ Python error: ${error}`);
        return res.status(500).json({ error: 'Image processing failed', details: error.trim() });
      }

      try {
        const response = JSON.parse(result.trim()); // Ensure clean JSON
        res.json(response);
      } catch (parseErr) {
        console.error('⚠️ JSON Parse Error:', parseErr);
        res.status(500).json({ error: 'Failed to parse Python output', rawOutput: result.trim() });
      }
    });

    // Handle execution errors
    pythonProcess.on('error', (err) => {
      console.error('🚨 Python execution error:', err);
      res.status(500).json({ error: 'Failed to start Python script', details: err.message });
    });

  } catch (err) {
    console.error('🔥 Server error:', err);
    res.status(500).json({ error: 'Server error', details: err.message });
  }
});

app.get('/', (req, res) => {
  res.send('Hello World!');
});

function startServer() {
  return new Promise((resolve, reject) => {
    const server = app.listen(port, (err) => {
      if (err) {
        reject(`Error starting server: ${err}`);
      }
      console.log(`✅ Server started on port: ${port}`);
      resolve(server);
    });
  });
}

function closeServer(server) {
  return new Promise((resolve, reject) => {
    server.close((err) => {
      if (err) {
        reject(`Error closing server: ${err}`);
      }
      console.log('🔴 Server closed');
      resolve();
    });
  });
}

module.exports = { startServer, closeServer };
