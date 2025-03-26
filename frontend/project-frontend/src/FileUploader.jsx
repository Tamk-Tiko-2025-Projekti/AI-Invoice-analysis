import React, { useState } from 'react';
import ShowData from './ShowData';

const FileUploader = () => {
  const [file, setFile] = useState(null)
  const [status, setStatus] = useState("idle")

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
      await fetch("http://localhost:3000/", {
        method: 'POST',
        body: formData
      });

      setStatus("success");
    } catch {
      setStatus("error");
    }
  }

  return (
    <div>
      <input type="file" onChange={handleFileChange}
      accept="image/*, application/pdf"/>
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

      <ShowData status={status}/>
    </div>
  );
}

export default FileUploader;
