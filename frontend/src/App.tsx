import { useState } from "react";
import "./App.css";

export default function App() {
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const checkHealth = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch("http://localhost:8080/health");
      if (!res.ok) throw new Error("Error en el servidor");
      const data = await res.json();
      setStatus(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Función para determinar la clase de color según el valor
  const getStatusClass = (value) => {
    const val = String(value).toLowerCase();
    if (val === "up" || val === "ok" || val === "online") return "status-up";
    if (val === "down" || val === "error" || val === "offline")
      return "status-down";
    return "status-default";
  };

  return (
    <div className="container">
      <h1>Service Health</h1>

      <button type="button" onClick={checkHealth} disabled={loading}>
        {loading ? "Verificando..." : "Chequear ahora"}
      </button>

      {error && <div className="error-msg">⚠️ {error}</div>}

      {status && (
        <div className="status-grid">
          {Object.entries(status).map(([key, value]) => (
            <div key={key} className="health-card">
              <span className="health-label">{key.replace("_", " ")}</span>
              <span className={`health-value ${getStatusClass(value)}`}>
                {String(value).toUpperCase()}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
