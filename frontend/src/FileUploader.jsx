import React, { useState } from 'react';
import DisplayData from './DisplayData';

const FileUploader = ({ testRun }) => {
  const [files, setFiles] = useState(null);
  const [status, setStatus] = useState('idle');
  const [data, setData] = useState([]);

  //Path for post request
  const path = `http://localhost:8080/testRun=${testRun.toString()}`

  /*
  In this function we handle the uploading of the files. First check if there are any files.
  If there aren't then we return out of the function. This shouldn't even be possible to get since
  upload button is not in the UI if there are no files selected, but just to play it safe it is there.
  
  Then we make format the formdata. We go through it in a for loop to append each file we have selected to it.
  After that we send the post request to the backend with the formdata and wait for the result.
  Once we get the result we set the response data into our data state and use it to showcase what the AI
  found in the invoices.

  If the response is not ok then we just empty the data array and display an error message.
   */
  async function handleFileUpload() {
    if (!files) return;

    setStatus('uploading');

    const formData = new FormData();
    for (let i = 0; i < files.length; i++) {
      formData.append(`file${i + 1}`, files);
    }

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
      <input type="file" onChange={(e) => { setFiles(e.target.files) }} accept="image/*, .pdf" multiple />

      {files && status !== 'uploading' && (
        <button onClick={handleFileUpload}>Upload</button>
      )}

      {status === 'uploading' && <p>Uploading...</p>}

      {status === 'error' && files.length == 1 && <p>Error uploading the file</p>}
      {status === 'error' && files.length > 1 && <p>Error uploading the files</p>}

      {status === 'success' && (
        <DisplayData data={data} />
      )}
    </div>
  );
};

export default FileUploader;
