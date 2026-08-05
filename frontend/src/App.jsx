import { useState, useEffect } from 'react'
import './index.css'

function App() {
  const [status, setStatus] = useState({ online: false, data: null })
  const [apiResponse, setApiResponse] = useState(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    fetch('/api/status')
      .then((res) => res.json())
      .then((data) => setStatus({ online: true, data }))
      .catch(() => setStatus({ online: false, data: null }))
  }, [])

  const testBackendApi = async () => {
    setLoading(true)
    try {
      const res = await fetch('/api/hello')
      const data = await res.json()
      setApiResponse(data)
    } catch (err) {
      setApiResponse({ error: 'Error de conexión con el backend' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app-container">
      <div className="glass-card">
        <header className="header">
          <div className="badge-group">
            <span className="badge">Spring Boot (Java)</span>
            <span className="badge">React + Vite</span>
            <span className="badge">Monorepo</span>
          </div>
          <h1 className="title">FairShare App</h1>
          <p className="subtitle">
            Plataforma con Frontend en React e Integración REST con Backend Java Spring Boot
          </p>
        </header>

        <div className="status-box">
          <div className="status-indicator">
            <span className={`dot ${status.online ? 'online' : ''}`}></span>
            <span>
              Backend Spring Boot:{' '}
              <strong>{status.online ? 'Conectado (UP)' : 'Desconectado'}</strong>
            </span>
          </div>
          {status.data && <span className="badge">{status.data.service}</span>}
        </div>

        <div className="actions">
          <button className="btn-primary" onClick={testBackendApi} disabled={loading}>
            {loading ? 'Consultando...' : '⚡ Probar Endpoint REST (/api/hello)'}
          </button>

          {apiResponse && (
            <div className="response-card">
              <strong>Respuesta del Backend:</strong>
              <pre>{JSON.stringify(apiResponse, null, 2)}</pre>
            </div>
          )}
        </div>

        <div className="arch-grid">
          <div className="arch-card">
            <h3>📂 Monorepo Desacoplado</h3>
            <p>
              `/frontend` contiene la app React impulsada por Vite, separada limpiamente del backend Java en la raíz.
            </p>
          </div>
          <div className="arch-card">
            <h3>🔄 Dev Proxying</h3>
            <p>
              Vite redirige automáticamente las peticiones `/api/*` al servidor Spring Boot en `localhost:8080`.
            </p>
          </div>
          <div className="arch-card">
            <h3>📦 Build Unificado</h3>
            <p>
              Soporta empaquetado directo a un único archivo `.jar` ejecutable mediante Maven para producción.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default App
