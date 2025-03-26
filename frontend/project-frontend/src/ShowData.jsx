import React, { useEffect, useState } from 'react';

const ShowData = ({ status }) => {
  const [data, setData] = useState([])

  useEffect(() => {
    const fetchData = async () => {
      if (status === "success") {
        const response = await fetch("http://localhost:3000/");
        const receivedData = await response.json();
        setData(receivedData);
      } else {
        setData([]);
      }
    };
    fetchData();
  }, [status]);

  return (
    <div>
      {status === "success" && (
        <div>
          <h2>JSON data:</h2>
          <pre>{JSON.stringify(data, null, 2)}</pre>
        </div>
      )}
    </div>
  );
}

export default ShowData;
