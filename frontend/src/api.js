// Thin wrapper around the backend REST API. Calls are same-origin "/api/..."
// (proxied to Spring Boot in dev, served behind the same host in prod).

export async function fetchStations() {
  const res = await fetch('/api/stations')
  if (!res.ok) throw new Error('Failed to load stations')
  return res.json()
}

export async function planRoute(origin, destination, preference) {
  const res = await fetch('/api/route', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ origin, destination, preference }),
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) {
    throw new Error(data.detail || data.error || 'Could not plan a route')
  }
  return data
}
