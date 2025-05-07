import './App.css'
import FileUploader from './FileUploader'
import Options from "./Options.jsx";
import { useState } from "react";

function App() {
  const [options, setOptions] = useState({
    testRun: false
  })

  return (
    <>
      <div>
        <h1>Automation of invoicing</h1>
        <h2>
          Only accepts pdf and photo files <br />
          Choose the file(s), press upload, get data
        </h2>
      </div>
      <FileUploader testRun={options.testRun} />
      <Options options={options} setOptions={setOptions} />
    </>
  )
}

export default App
