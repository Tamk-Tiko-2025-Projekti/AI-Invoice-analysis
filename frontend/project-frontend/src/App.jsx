import { useState } from 'react'
import './App.css'
import FileSelection from './FileUploader'

function App() {
  return (
    <>
      <div>
        <h1>To do:</h1>
        <ul>
          <li>Make fileuploader upload to our backend</li>
          <li>Make another page to fetch the data from backend and showcase it</li>
        </ul>
      </div>
      <FileSelection/>
    </>
  )
}

export default App
