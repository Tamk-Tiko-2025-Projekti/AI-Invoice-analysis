import React from 'react';

const Options = ({options, setOptions}) => {
  return (
    <div>
      <h2>Options</h2>
      Test run: <input
      type={"checkbox"}
      title={"Test run"}
      name={"Test run"}
      id={"testRun"}
      checked={options.testRun}
      onChange={(e) => {
        setOptions({...options, testRun: e.target.checked})
      }}/>
    </div>
  );
}

export default Options;
