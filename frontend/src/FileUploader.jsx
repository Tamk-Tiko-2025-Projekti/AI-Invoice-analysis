import React, { useState, useEffect } from 'react';
import DisplayData from './DisplayData';

const FileUploader = ({ testRun }) => {
  const [file, setFile] = useState(null);
  const [fileType, setFileType] = useState('');
  const [status, setStatus] = useState('idle');
  const [data, setData] = useState([]);
  const [path, setPath] = useState('')

  useEffect(() => {
    if (file) {
      if (file.type === 'application/pdf') {
        setFileType('pdf');
        setPath(`http://localhost:8080/pdf?testRun=${testRun.toString()}`);
      } else if (file.type.startsWith('image/')) {
        setFileType('image');
        setPath(`http://localhost:8080/image?testRun=${testRun.toString()}`);
      } else {
        setFileType('');
        setPath('');
      }
    } else {
      setFileType('');
      setPath('');
    }
  }, [file, testRun]);

  function handleFileChange(e) {
    if (e.target.files) {
      setFile(e.target.files[0]);
      console.log('Selected file:', e.target.files[0]);
    }
  }

  async function handleFileUpload() {
    if (!file || !path) return;

    setStatus('uploading');

    const formData = new FormData();
    formData.append(fileType, file);
    console.log('FormData:', formData);

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
      <input type="file" onChange={handleFileChange} accept="image/*, .pdf" multiple />
      {file && (
        <div>
          <p>File name: {file.name}</p>
          <p>Type: {file.type}</p>
        </div>
      )}

      {file && status !== 'uploading' && (
        <button onClick={handleFileUpload}>Upload</button>
      )}

      {status === 'uploading' && <p>File is uploading...</p>}

      {status === 'error' && <p>Error uploading the file</p>}

      {status === 'success' && (
        <DisplayData data={data} />
      )}
    </div>
  );
};

export default FileUploader;
