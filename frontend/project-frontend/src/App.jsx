import './App.css'
import FileUploader from './FileUploader'

function App() {
  return (
    <>
      <div>
        <h1>Laskutuksen automatisointi</h1>
        <h2>Hyväksyy vain kuvia taikka pdf tiedostoja</h2>
        <h2>Valitse tiedosto, paina upload, saa JSON dataa</h2>
      </div>
      <FileUploader/>
    </>
  )
}

export default App
