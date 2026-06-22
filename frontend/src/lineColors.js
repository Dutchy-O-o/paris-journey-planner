// Official-ish Paris Métro & RER line colours, keyed by route_short_name.
const LINE_COLORS = {
  '1': '#FFCE00',
  '2': '#0064B0',
  '3': '#9F9825',
  '3bis': '#98D4E2',
  '4': '#C04191',
  '5': '#F28E42',
  '6': '#83C491',
  '7': '#F3A4BA',
  '7bis': '#83C491',
  '8': '#CEADD2',
  '9': '#D5C900',
  '10': '#E3B32A',
  '11': '#8D5E2A',
  '12': '#00814F',
  '13': '#98D4E2',
  '14': '#662483',
  A: '#E2231A',
  B: '#7BA3DC',
  C: '#FFCE00',
  D: '#008B5B',
  E: '#C04191',
}

const WALK_COLOR = '#8A8A8A'

export function lineColor(line, walking) {
  if (walking) return WALK_COLOR
  return LINE_COLORS[line] || '#444'
}

// Yellow lines need a dark label for contrast; everything else gets white.
export function lineTextColor(line, walking) {
  const dark = ['1', '9', 'C']
  if (!walking && dark.includes(line)) return '#1a1a1a'
  return '#fff'
}
