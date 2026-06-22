import { lineColor, lineTextColor } from '../lineColors'

function formatDuration(seconds) {
  const mins = Math.round(seconds / 60)
  if (mins < 60) return `${mins} min`
  const h = Math.floor(mins / 60)
  const m = mins % 60
  return m ? `${h} h ${m} min` : `${h} h`
}

function LineBadge({ line, walking }) {
  if (walking) {
    return <span className="badge badge-walk">🚶 Walk</span>
  }
  return (
    <span
      className="badge"
      style={{ background: lineColor(line, false), color: lineTextColor(line, false) }}
    >
      {line}
    </span>
  )
}

export default function RoutePanel({ route }) {
  if (!route) return null

  return (
    <div className="route-panel">
      <div className="route-summary">
        <div>
          <span className="summary-value">{formatDuration(route.totalSeconds)}</span>
          <span className="summary-label">travel time</span>
        </div>
        <div>
          <span className="summary-value">{route.totalStops}</span>
          <span className="summary-label">stops</span>
        </div>
        <div>
          <span className="summary-value">{route.segments.filter((s) => !s.walking).length}</span>
          <span className="summary-label">lines</span>
        </div>
      </div>

      <ol className="segments">
        {route.segments.map((seg, i) => {
          const from = seg.stations[0]
          const to = seg.stations[seg.stations.length - 1]
          return (
            <li key={i} className="segment">
              <LineBadge line={seg.line} walking={seg.walking} />
              <div className="segment-body">
                <div className="segment-stations">
                  <strong>{from.name}</strong> → <strong>{to.name}</strong>
                </div>
                <div className="segment-meta">
                  {seg.walking ? 'transfer on foot' : `${seg.stationCount ?? seg.stations.length - 1} stop(s)`}
                </div>
              </div>
            </li>
          )
        })}
      </ol>
    </div>
  )
}
