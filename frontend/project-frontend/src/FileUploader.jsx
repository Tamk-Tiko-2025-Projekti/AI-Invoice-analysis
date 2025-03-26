import React, { useState } from 'react';
import ShowData from './ShowData';

const FileUploader = () => {
  const [file, setFile] = useState(null)
  const [status, setStatus] = useState("idle")
  const [data, setData] = useState([])

  function handleFileChange(e) {
    if (e.target.files) {
      setFile(e.target.files[0]);
    }
  }

  async function handleFileUpload() {
    if (!file) return;

    setStatus("uploading")

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch("http://localhost:3000/", {
        method: 'POST',
        body: formData
      });

      if (response.ok) {
        const result = await response.json();
        setData(result);
        setStatus("success");
      } else {
        setData([]);
        setStatus("error");
      }
    } catch {
      setStatus("error");
    }
  }

  return (
    <div>
      <input type="file" onChange={handleFileChange}
      accept="image/*"/>
      {file && (
        <div>
          <p>File name: {file.name}</p>
          <p>Type: {file.type}</p>
        </div>
      )}

      {file && status !== "uploading" && (
        <button onClick={handleFileUpload}>Upload</button>
      )}

      {status === "uploading" && (
        <p>File is uploading...</p>
      )}

      {status === "error" && (
        <p>Error uploading the file</p>
      )}

      {status === "success" && (
        <div>
          <h2>JSON data:</h2>
          <pre>{JSON.stringify(data, null, 2)}</pre>
        </div>
      )}
    </div>
  );
}

export default FileUploader;
