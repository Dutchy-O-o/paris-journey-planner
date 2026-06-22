import { useEffect, useMemo } from 'react'
import {
  MapContainer,
  TileLayer,
  Polyline,
  CircleMarker,
  Tooltip,
  useMap,
  useMapEvents,
} from 'react-leaflet'
import { lineColor } from '../lineColors'

const PARIS_CENTER = [48.8566, 2.3522]

// Nearest station to a clicked point (squared lat/lon distance is fine at city scale).
function nearestStation(stations, lat, lng) {
  let best = null
  let bestDist = Infinity
  for (const s of stations) {
    if (s.latitude == null || s.longitude == null) continue
    const d = (s.latitude - lat) ** 2 + (s.longitude - lng) ** 2
    if (d < bestDist) {
      bestDist = d
      best = s
    }
  }
  return best
}

// Turns a map click into a station pick.
function ClickPicker({ stations, onPick }) {
  useMapEvents({
    click(e) {
      const s = nearestStation(stations, e.latlng.lat, e.latlng.lng)
      if (s) onPick(s.name)
    },
  })
  return null
}

// Pulls the route's station coordinates into [lat, lon] pairs, skipping any gaps.
function segmentPositions(segment) {
  return segment.stations
    .filter((s) => s.latitude != null && s.longitude != null)
    .map((s) => [s.latitude, s.longitude])
}

// Imperatively re-fits the viewport whenever the drawn route changes.
function FitToRoute({ route }) {
  const map = useMap()
  useEffect(() => {
    if (!route) {
      map.setView(PARIS_CENTER, 12)
      return
    }
    const points = route.segments.flatMap(segmentPositions)
    if (points.length > 0) {
      map.fitBounds(points, { padding: [60, 60] })
    }
  }, [route, map])
  return null
}

const ORIGIN_COLOR = '#1a9850'
const DESTINATION_COLOR = '#d73027'
const STATION_COLOR = '#5b6b80'

export default function MapView({ stations, route, origin, destination, onPickStation }) {
  // Faint dots for the whole network, so the map reads as a transit map at rest.
  const networkDots = useMemo(
    () => stations.filter((s) => s.latitude != null && s.longitude != null),
    [stations],
  )

  // The endpoints and transfer stations get emphasised markers.
  const keyStops = useMemo(() => {
    if (!route) return []
    const stops = []
    route.segments.forEach((seg, i) => {
      const first = seg.stations[0]
      const last = seg.stations[seg.stations.length - 1]
      if (i === 0) stops.push({ ...first, kind: 'origin' })
      else stops.push({ ...first, kind: 'transfer' })
      if (i === route.segments.length - 1) stops.push({ ...last, kind: 'destination' })
    })
    return stops
  }, [route])

  return (
    <MapContainer center={PARIS_CENTER} zoom={12} className="map" zoomControl={false}>
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/attributions">CARTO</a>'
        url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
      />

      {networkDots.map((s) => {
        const isOrigin = s.name === origin
        const isDestination = s.name === destination
        const selected = isOrigin || isDestination
        const fillColor = isOrigin ? ORIGIN_COLOR : isDestination ? DESTINATION_COLOR : STATION_COLOR
        return (
          <CircleMarker
            key={`net-${s.name}`}
            center={[s.latitude, s.longitude]}
            radius={selected ? 7 : 3.5}
            className={selected ? 'net-dot net-dot-selected' : 'net-dot'}
            pathOptions={{
              color: '#ffffff',
              weight: selected ? 2.5 : 1.2,
              fillColor,
              fillOpacity: selected ? 1 : 0.85,
            }}
          >
            <Tooltip direction="top" offset={[0, -4]}>
              {isOrigin ? `From: ${s.name}` : isDestination ? `To: ${s.name}` : s.name}
            </Tooltip>
          </CircleMarker>
        )
      })}

      {onPickStation && <ClickPicker stations={networkDots} onPick={onPickStation} />}

      {route &&
        route.segments.map((seg, i) => {
          const positions = segmentPositions(seg)
          const color = lineColor(seg.line, seg.walking)
          return (
            <Polyline
              key={`seg-${i}`}
              positions={positions}
              pathOptions={{
                color,
                weight: 6,
                opacity: 0.9,
                dashArray: seg.walking ? '2 8' : null,
                lineCap: 'round',
                lineJoin: 'round',
              }}
            />
          )
        })}

      {route &&
        route.segments.flatMap((seg, i) =>
          seg.stations
            .filter((s) => s.latitude != null)
            .map((s, j) => (
              <CircleMarker
                key={`stop-${i}-${j}`}
                center={[s.latitude, s.longitude]}
                radius={4}
                pathOptions={{
                  color: '#fff',
                  weight: 2,
                  fillColor: lineColor(seg.line, seg.walking),
                  fillOpacity: 1,
                }}
              >
                <Tooltip>{s.name}</Tooltip>
              </CircleMarker>
            )),
        )}

      {keyStops.map((s, i) => (
        <CircleMarker
          key={`key-${s.kind}-${i}`}
          center={[s.latitude, s.longitude]}
          radius={s.kind === 'transfer' ? 6 : 8}
          pathOptions={{
            color: '#1a1a1a',
            weight: 3,
            fillColor:
              s.kind === 'origin' ? '#1a9850' : s.kind === 'destination' ? '#d73027' : '#fff',
            fillOpacity: 1,
          }}
        >
          <Tooltip permanent direction="top" offset={[0, -8]} className="stop-label">
            {s.name}
          </Tooltip>
        </CircleMarker>
      ))}

      <FitToRoute route={route} />
    </MapContainer>
  )
}
