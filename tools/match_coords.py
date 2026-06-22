#!/usr/bin/env python3
"""Join our station names against the IDFM 'emplacement des gares' open dataset to
attach latitude/longitude. Matching is done on a normalised key (accent-folded,
separator-flattened) and coordinates for the same station are averaged.

Outputs:
  - backend/src/main/resources/data/station_coords.csv  (stop_name,latitude,longitude)
  - prints coverage and the list of unmatched stations for manual fixup.
"""
import csv
import sys
import unicodedata
from collections import defaultdict

OURS = "backend/src/main/resources/data/Paris_RER_Metro_v2.csv"
IDFM = "tools/idfm_gares.csv"
OUT = "backend/src/main/resources/data/station_coords.csv"


def _fold(name: str) -> str:
    s = unicodedata.normalize("NFKD", name)
    s = "".join(c for c in s if not unicodedata.combining(c))
    s = s.lower()
    for ch in "-–—'’/.":
        s = s.replace(ch, " ")
    return s


def norm(name: str) -> str:
    s = _fold(name).replace("(", " ").replace(")", " ")
    s = " ".join(s.split())
    s = s.replace(" rer", "").replace("gare de ", "gare ")
    return s.strip()


def no_initials(key: str) -> str:
    """Drop single-letter tokens, e.g. 'franklin d roosevelt' -> 'franklin roosevelt'."""
    return " ".join(t for t in key.split() if len(t) > 1)


def candidate_keys(name: str):
    """Ordered keys to try for one of our station names."""
    keys = []
    base = norm(name)
    # strip a trailing/embedded parenthetical subtitle: "Voltaire (Léon Blum)" -> "Voltaire"
    before_paren = name.split("(", 1)[0]
    paren = norm(before_paren)
    for k in (base, paren, no_initials(base), no_initials(paren)):
        if k and k not in keys:
            keys.append(k)
    return keys


def read_our_stations(path):
    names = []
    with open(path, encoding="utf-8-sig") as f:
        reader = csv.reader(f)
        next(reader, None)  # header
        seen = set()
        for row in reader:
            if len(row) > 1:
                n = row[1].strip()
                if n and n not in seen:
                    seen.add(n)
                    names.append(n)
    return names


def read_idfm(path):
    """Return normalised-name -> list[(lat, lon)] from nom_gares (and nom_zdc fallback)."""
    coords = defaultdict(list)
    with open(path, encoding="utf-8-sig") as f:
        reader = csv.reader(f, delimiter=";", quotechar='"')
        header = next(reader)
        idx_geo = header.index("geo_point_2d")
        idx_nom = header.index("nom_gares")
        idx_zdc = header.index("nom_zdc")
        for row in reader:
            if len(row) <= max(idx_geo, idx_nom, idx_zdc):
                continue
            geo = row[idx_geo].strip()
            if not geo or "," not in geo:
                continue
            lat_s, lon_s = geo.split(",", 1)
            try:
                lat, lon = float(lat_s), float(lon_s)
            except ValueError:
                continue
            for field in (row[idx_nom], row[idx_zdc]):
                key = norm(field)
                if key:
                    coords[key].append((lat, lon))
                    ni = no_initials(key)
                    if ni and ni != key:
                        coords[ni].append((lat, lon))
    return coords


# Hand-resolved coordinates for stations whose IDFM name differs too much to match
# automatically. Keyed by our normalised name (see norm()).
MANUAL = {
    "pont de levallois becon": (48.897154, 2.280414),       # IDFM: "Pont de Levallois"
    "aubervilliers pantin 4 chemins": (48.903784, 2.392221),  # IDFM: "...Quatre Chemins"
    "villeparisis": (48.953148, 2.603135),                  # IDFM: "Villeparisis-Mitry-le-Neuf"
    "blanc mesnil": (48.932305, 2.475674),                  # IDFM: "Le Blanc-Mesnil"
    # Source CSV has a typo: "Saint-Rémy Iès-Chevreuse" (capital I instead of "l").
    "saint remy ies chevreuse": (48.702751, 2.071243),      # IDFM: "Saint-Rémy-lès-Chevreuse"
}


def main():
    ours = read_our_stations(OURS)
    idfm = read_idfm(IDFM)
    for key, (lat, lon) in MANUAL.items():
        idfm[key] = [(lat, lon)]

    matched, unmatched = {}, []
    for name in ours:
        pts = None
        for key in candidate_keys(name):
            if key in idfm:
                pts = idfm[key]
                break
        if pts:
            lat = sum(p[0] for p in pts) / len(pts)
            lon = sum(p[1] for p in pts) / len(pts)
            matched[name] = (lat, lon)
        else:
            unmatched.append(name)

    with open(OUT, "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f)
        w.writerow(["stop_name", "latitude", "longitude"])
        for name in ours:
            if name in matched:
                lat, lon = matched[name]
                w.writerow([name, f"{lat:.6f}", f"{lon:.6f}"])

    total = len(ours)
    print(f"Matched {len(matched)}/{total} ({100*len(matched)/total:.1f}%)")
    print(f"Unmatched {len(unmatched)}:")
    for n in unmatched:
        print(f"  - {n}")


if __name__ == "__main__":
    main()
