import requests
import csv
import time

# List of coordinates (lat, lon)
coords = [
    (43.358200,-80.316658),
    (43.468914,-80.541843),
    (43.473670,-80.545360),
    (43.470500,-80.543000),
    (43.472000,-80.541000),
    (43.466300,-80.544980),
    (43.472800,-80.542246),
    (43.470050,-80.539547),
    (43.470938,-80.540333),
    (43.471680,-80.540700),
    (43.472400,-80.542600),
    (43.473037,-80.539546),
    (43.471387,-80.542751),
    (43.469700,-80.541700),
    (43.467985,-80.543173),
    (43.468279,-80.543394),
    (43.369733,-80.540925),
    (43.467754,-80.541509),
    (43.469720,-80.542282),
    (43.472107,-80.543912),
    (43.468949,-80.542966),
    (43.475791,-80.498959),
    (43.452793,-80.498959),
    (43.471194,-80.544077),
    (43.470264,-80.554148),
    (43.470302,-80.540728),
    (43.471663,-80.545287),
    (43.470922,-80.543275),
    (43.469007,-80.541295),
    (43.473624,-80.545363),
    (43.471594,-80.549903)
]

OVERPASS_URL = "http://overpass-api.de/api/interpreter"

def get_building_name(lat, lon, radius=25):
    """Query OSM Overpass API for nearby building names."""
    query = f"""
    [out:json];
    (
      way(around:{radius},{lat},{lon})[building][name];
      relation(around:{radius},{lat},{lon})[building][name];
    );
    out center 1;
    """
    try:
        r = requests.post(OVERPASS_URL, data={'data': query}, timeout=15)
        r.raise_for_status()
        data = r.json()
        if "elements" in data and data["elements"]:
            el = min(
                data["elements"],
                key=lambda e: ((e.get("center", {}).get("lat", lat) - lat) ** 2 +
                               (e.get("center", {}).get("lon", lon) - lon) ** 2)
            )
            return el["tags"].get("name")
    except Exception as e:
        print(f"⚠️ Error fetching OSM data for ({lat},{lon}): {e}")
    return ""

if __name__ == "__main__":
    with open("building.csv", "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["name"])  # header

        for i, (lat, lon) in enumerate(coords, start=1):
            print(f"Looking up building at ({lat}, {lon})...")
            name = get_building_name(lat, lon)
            if not name:
                name = ""  # fallback if nothing found
            writer.writerow([name])
            time.sleep(2)  # avoid API overload

    print("\n✅ Done! Generated building.csv")
