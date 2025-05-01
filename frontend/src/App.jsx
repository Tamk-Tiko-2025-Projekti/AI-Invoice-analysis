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
        <h1>Laskutuksen automatisointi</h1>
        <h2>
          Hyväksyy kuvia sekä pdf tiedostoja <br />
          Valitse tiedosto(t), paina upload, saa JSON dataa
        </h2>
      </div>
      <FileUploader testRun={options.testRun} />
      <Options options={options} setOptions={setOptions} />
    </>
  )
}

export default App
