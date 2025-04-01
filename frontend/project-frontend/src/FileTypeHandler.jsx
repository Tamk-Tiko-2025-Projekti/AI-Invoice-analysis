import React, { useState } from 'react';

export default function FileTypeHandler({ file, setFileType }) {
  const [isPdfChecked, setIsPdfChecked] = useState(false);
  const [isImageChecked, setIsImageChecked] = useState(false);

  const handleCheckboxChange = (e) => {
    const { name, checked } = e.target;

    if (name === 'pdf') {
      setIsPdfChecked(checked);
      setIsImageChecked(false);

      console.log("pdf checked");
      if (checked && file.type === 'application/pdf') {
        setFileType('PDF');
        console.log("File is pdf");
      }
    } else if (name === 'image') {
      setIsImageChecked(checked);
      setIsPdfChecked(false);

      console.log("image checked");
      if (checked && file.type.startsWith('image/')) {
        setFileType('Image');
        console.log("File is image");
      }
    }
  };

  return (
    <>
      <div>Valitse kumpaa tiedostoa olet lataamassa:</div>
      <div>
        <input
          type="checkbox"
          name="pdf"
          checked={isPdfChecked}
          onChange={handleCheckboxChange}
        />
        <label>PDF</label>
      </div>
      <div>
        <input
          type="checkbox"
          name="image"
          checked={isImageChecked}
          onChange={handleCheckboxChange}
        />
        <label>Image</label>
      </div>
    </>
  );
}
