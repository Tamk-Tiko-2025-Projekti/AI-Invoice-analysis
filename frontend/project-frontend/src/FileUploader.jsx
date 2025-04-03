import React, { useState, useEffect } from 'react';
import FileTypeHandler from './FileTypeHandler';
import DisplayData from './DisplayData';

const FileUploader = ({ testRun }) => {
  const [file, setFile] = useState(null);
  const [fileType, setFileType] = useState('');
  const [status, setStatus] = useState('idle');
  const [data, setData] = useState([]);
  const [path, setPath] = useState('')

  useEffect(() => {
    // if (fileType === 'pdf') {
    if (file && file.type === 'application/pdf') {
      setPath(`http://localhost:8080/pdf?testRun=${testRun.toString()}`);
    } else if (fileType === 'image') {
      setFileType('pdf');
    // } else if (fileType === 'image') {
    } else if (file && file.type.startsWith('image/')) {
      setPath(`http://localhost:8080/image?testRun=${testRun.toString()}`);
      setFileType('image');
    } else {
      setPath('');
    }
  }, [file, fileType, testRun]);

  function handleFileChange(e) {
    if (e.target.files) {
      setFile(e.target.files[0]);
    }
  }

  async function handleFileUpload() {
    if (!file || !path) return;

    setStatus('uploading');

    const formData = new FormData();
    formData.append(fileType, file);

    try {
      const response = await fetch(path, {
        method: 'POST',
        body: formData,
      });
      if (response.ok) {
        const result = await response.json();
        console.log('Result:', result);
        setData(result);
        setStatus('success');
      } else {
        setData([]);
        setStatus('error');
      }
    } catch {
      setStatus('error');
    }
  }

  return (
    <div>
      <input type="file" onChange={handleFileChange} accept="image/*, .pdf" />
      {file && (
        <div>
          <p>File name: {file.name}</p>
          <p>Type: {file.type}</p>
        </div>
      )}

      <FileTypeHandler file={file} setFileType={setFileType} />

      {file && status !== 'uploading' && (
        <button onClick={handleFileUpload}>Upload</button>
      )}

      {status === 'uploading' && <p>File is uploading...</p>}

      {status === 'error' && <p>Error uploading the file</p>}

      {status === 'success' && (
        <div>
          <h2>JSON data:</h2>
          <pre>{JSON.stringify(data, null, 2)}</pre>
          <DisplayData data={data} />
        </div>
      )}
    </div>
  );
};

export default FileUploader;
