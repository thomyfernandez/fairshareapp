import { useState } from 'react'

function App() {
  const [response, setResponse] = useState(null)

  // Metodo para consultar un endpoint y guardar el resultado en el estado
  const callEndpoint = async (url) => {
    try {
      const res = await fetch(url)
      const data = await res.json()
      setResponse(data)
    } catch (error) {
      setResponse({ error: 'Error al conectar con la API' })
    }
  }

  return (
    <div>
      <h1>FairShare App</h1>
      <p>Probador simple de endpoints REST:</p>

      <div>
        <button onClick={() => callEndpoint('/api/status')}>
          Consultar /api/status
        </button>
        <button onClick={() => callEndpoint('/api/hello')}>
          Consultar /api/hello
        </button>
      </div>

      {response && (
        <div>
          <h3>Respuesta:</h3>
          <pre>{JSON.stringify(response, null, 2)}</pre>
        </div>
      )}
    </div>
  )
}

export default App
