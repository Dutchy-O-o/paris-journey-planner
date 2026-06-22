import { useEffect, useState } from 'react'
import MapView from './components/MapView'
import RoutePanel from './components/RoutePanel'
import { fetchStations, planRoute } from './api'
import './App.css'

export default function App() {
  const [stations, setStations] = useState([])
  const [origin, setOrigin] = useState('')
  const [destination, setDestination] = useState('')
  const [preference, setPreference] = useState('TIME')
  const [route, setRoute] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  // Which field a map click should fill next.
  const [activeField, setActiveField] = useState('origin')

  useEffect(() => {
    fetchStations()
      .then((list) => {
        setStations(list)
        // Support shareable deep links: /?from=Nation&to=Bastille&pref=TIME
        const params = new URLSearchParams(window.location.search)
        const from = params.get('from')
        const to = params.get('to')
        const pref = params.get('pref')
        if (pref === 'TIME' || pref === 'STOPS') setPreference(pref)
        if (from) setOrigin(from)
        if (to) setDestination(to)
        const names = list.map((s) => s.name)
        if (from && to && names.includes(from) && names.includes(to)) {
          planRoute(from, to, pref || 'TIME').then(setRoute).catch((e) => setError(e.message))
        }
      })
      .catch(() => setError('Could not load the station list. Is the backend running?'))
  }, [])

  const names = stations.map((s) => s.name)
  const canSubmit = names.includes(origin) && names.includes(destination) && origin !== destination

  async function onSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const result = await planRoute(origin, destination, preference)
      setRoute(result)
      const qs = new URLSearchParams({ from: origin, to: destination, pref: preference })
      window.history.replaceState(null, '', `?${qs}`)
    } catch (err) {
      setRoute(null)
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  function swap() {
    setOrigin(destination)
    setDestination(origin)
  }

  // Fill the currently active field from a map click, then advance From → To.
  function pickFromMap(name) {
    if (activeField === 'origin') {
      setOrigin(name)
      if (!destination) setActiveField('destination')
    } else {
      setDestination(name)
    }
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <header className="brand">
          <h1>Paris Journey Planner</h1>
          <p>Shortest path across the Métro &amp; RER — {stations.length || '…'} stations.</p>
        </header>

        <form className="search" onSubmit={onSubmit}>
          <datalist id="station-list">
            {names.map((n) => (
              <option key={n} value={n} />
            ))}
          </datalist>

          <label className={activeField === 'origin' ? 'field active' : 'field'}>
            <span className="dot dot-origin" />
            <input
              list="station-list"
              placeholder="From station"
              value={origin}
              onFocus={() => setActiveField('origin')}
              onChange={(e) => setOrigin(e.target.value)}
            />
          </label>

          <button type="button" className="swap" onClick={swap} title="Swap">
            ⇅
          </button>

          <label className={activeField === 'destination' ? 'field active' : 'field'}>
            <span className="dot dot-dest" />
            <input
              list="station-list"
              placeholder="To station"
              value={destination}
              onFocus={() => setActiveField('destination')}
              onChange={(e) => setDestination(e.target.value)}
            />
          </label>

          <p className="map-hint">
            📍 Tip: click anywhere on the map to set the{' '}
            <strong>{activeField === 'origin' ? 'From' : 'To'}</strong> station.
          </p>

          <div className="prefs">
            <label className={preference === 'TIME' ? 'pref active' : 'pref'}>
              <input
                type="radio"
                name="pref"
                checked={preference === 'TIME'}
                onChange={() => setPreference('TIME')}
              />
              Fastest
            </label>
            <label className={preference === 'STOPS' ? 'pref active' : 'pref'}>
              <input
                type="radio"
                name="pref"
                checked={preference === 'STOPS'}
                onChange={() => setPreference('STOPS')}
              />
              Fewer stops
            </label>
          </div>

          <button type="submit" className="go" disabled={!canSubmit || loading}>
            {loading ? 'Planning…' : 'Plan journey'}
          </button>
        </form>

        {error && <div className="error">{error}</div>}

        <RoutePanel route={route} />

        <footer className="credits">
          Dijkstra over a custom graph ADT · data: Île-de-France Mobilités
        </footer>
      </aside>

      <main className="map-wrap">
        <MapView
          stations={stations}
          route={route}
          origin={origin}
          destination={destination}
          onPickStation={pickFromMap}
        />
      </main>
    </div>
  )
}
