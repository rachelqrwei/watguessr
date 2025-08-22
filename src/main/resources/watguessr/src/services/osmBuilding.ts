export interface OSMElement {
  id: number;
  type: "node" | "way" | "relation";
  lat?: number;
  lon?: number;
  tags?: Record<string, string>;
  center?: { lat: number; lon: number };
}

function distanceMeters(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371000; // meters
  const toRad = (deg: number) => (deg * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

export async function getNearbyName(
  lat: number,
  lon: number,
  radius: number = 25
): Promise<string | null> {
  const query = `
    [out:json];
    (
      way(around:${radius},${lat},${lon})["building"]["name"];
      relation(around:${radius},${lat},${lon})["building"]["name"];
      node(around:${radius},${lat},${lon})["name"];
      way(around:${radius},${lat},${lon})["name"];
      relation(around:${radius},${lat},${lon})["name"];
    );
    out tags center;
  `;

  try {
    const response = await fetch("https://overpass-api.de/api/interpreter", {
      method: "POST",
      body: query,
    });

    const data = await response.json();
    const elements = data.elements as OSMElement[]; // <-- fixed here
    if (!elements || elements.length === 0) return null;

    const elementsWithCoords = elements
      .map((el) => {
        const cLat = el.center?.lat ?? el.lat;
        const cLon = el.center?.lon ?? el.lon;
        if (cLat == null || cLon == null || !el.tags?.name) return null;
        return { el, dist: distanceMeters(lat, lon, cLat, cLon) };
      })
      .filter((v): v is { el: OSMElement; dist: number } => v !== null);

    // 1. Prefer building ways/relations first
    const building = elementsWithCoords.find(
      ({ el }) =>
        (el.type === "way" || el.type === "relation") &&
        el.tags?.building &&
        el.tags?.name
    );
    if (building) return building.el.tags!.name;

    // 2. Otherwise pick the closest remaining element
    if (elementsWithCoords.length > 0) {
      elementsWithCoords.sort((a, b) => a.dist - b.dist);
      return elementsWithCoords[0].el.tags!.name;
    }

    return null; // nothing found
  } catch (err) {
    console.error("Error querying OSM:", err);
    return null;
  }
}
