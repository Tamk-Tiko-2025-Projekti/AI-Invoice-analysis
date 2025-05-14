import React, { useState } from 'react';
import DisplayData from './DisplayData';

const FileUploader = ({ testRun }) => {
  const [files, setFiles] = useState(null);
  const [fileNames, setFileNames] = useState([]);
  const [status, setStatus] = useState('idle');
  const [data, setData] = useState([]);

  //Path for post request
  const path = `http://localhost:8080/files?testRun=${testRun.toString()}`

  /*
  File input on change event function. Here we setFiles to the selected files
  and also map the file names into setFileNames state, so we can display
  the file names in DisplayData component
   */
  const handleFileSelection = (e) => {
    const selectedFiles = e.target.files;
    setFiles(selectedFiles);
    setFileNames(Array.from(selectedFiles).map((file) => file.name));
  }

  /*
  In this function we handle the uploading of the files. First check if there are any files.
  If there aren't then we return out of the function. This shouldn't even be possible to get since
  upload button is not in the UI if there are no files selected, but just to play it safe it is there.
  
  Then we make format the formdata. We go through it in a foreach loop to append each file we have selected to it.
  After that we send the post request to the backend with the formdata and wait for the result.
  Once we get the result we set the response data into our data state and use it to showcase what the AI
  found in the invoices.

  If the response is not ok then we just empty the data array and display an error message.
   */
  async function handleFileUpload() {
    if (!files || files.length === 0) return;

    setStatus('uploading');

    const formData = new FormData();
    Array.from(files).forEach(file => {
      formData.append('files', file);

    });

    try {
      const response = await fetch(path, {
        method: 'POST',
        body: formData,
      });
      if (response.ok) {
        const result = await response.json();
        console.log('Result:', result);
        const parsedData = result.map(item => {
          if (typeof item === 'string') {
            try {
              return JSON.parse(item);
            } catch (error) {
              console.error('Error parsing JSON:', error);
              return null;
            }
          }
          return item;
        });
        setData(parsedData);
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
      <input
        type="file"
        onChange={handleFileSelection}
        accept="image/*, .pdf"
        multiple
      />

      {files && status !== 'uploading' && (
        <button onClick={handleFileUpload}>Upload</button>
      )}

      {status === 'uploading' && <p>Uploading...</p>}

      {status === 'error' && files.length === 1 && <p>Error uploading the file</p>}
      {status === 'error' && files.length > 1 && <p>Error uploading the files</p>}

      {status === 'success' && (
        <DisplayData data={data} fileNames={fileNames} />
      )}
    </div>
  );
};

export default FileUploader;
