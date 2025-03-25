import './App.css'
import FileUploader from './FileUploader'

function App() {
  return (
    <>
      <div>
        <h1>To do:</h1>
        <ul>
          <li>Make fileuploader upload to our backend: localhost:3000</li>
        </ul>
      </div>
      <FileUploader/>
    </>
  )
}

export default App
