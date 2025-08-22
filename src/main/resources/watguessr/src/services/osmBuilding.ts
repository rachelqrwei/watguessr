import { openDB } from 'idb';

const lat = 43.47247223467783;   // center latitude
const lon = -80.54478250141877;  // center longitude
const radius = 1000;              // radius in meters

export interface OSMElement {
  id: number;
  type: "node" | "way" | "relation";
  lat?: number;
  lon?: number;
  tags?: Record<string, string>;
  center?: { lat: number; lon: number };
}

// Initialize IndexedDB
const dbPromise = openDB('osmDB', 1, {
  upgrade(db) {
    db.createObjectStore('buildings', { keyPath: 'id' });
  },
});

async function fetchOSMBuildings(): Promise<OSMElement[] | undefined> {
  const query = `
    [out:json][timeout:25];
    (
      way(around:${radius},${lat},${lon})["building"]["name"];
      relation(around:${radius},${lat},${lon})["building"]["name"];
    );
    out tags center;
  `;

  try {
    const response = await fetch("https://overpass-api.de/api/interpreter", {
      method: "POST",
      body: query,
    });

    const data = await response.json();
    const elements: OSMElement[] = data.elements;

    if (!elements || elements.length === 0) {
      return;
    }

    return elements;
  } catch (err) {
    console.error("Error fetching OSM data:", err);
  }
}

// Cache OSM data into IndexedDB
export async function fetchAndCacheOSMBuildings() {
  const elements = await fetchOSMBuildings();
  if (!elements) return;

  const db = await dbPromise;
  const tx = db.transaction('buildings', 'readwrite');
  for (const el of elements) {
    tx.store.put(el);
  }
  await tx.done;
}

// Distance helper
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

/**
 * Returns nearby building/feature name from cached IndexedDB
 */
export async function getNearbyName(lat: number, lon: number, radius: number = 40): Promise<string | null> {
  const db = await dbPromise;
  const elements: OSMElement[] = await db.getAll('buildings');

  // 1. Compute distance for all elements with coordinates and name
  const elementsWithCoords = elements
    .map(el => {
      const cLat = el.center?.lat ?? el.lat;
      const cLon = el.center?.lon ?? el.lon;
      if (!cLat || !cLon || !el.tags?.name) return null;
      return { el, dist: distanceMeters(lat, lon, cLat, cLon) };
    })
    .filter((v): v is { el: OSMElement; dist: number } => v !== null)
    .filter(({ dist }) => dist <= radius); // only consider nearby

  if (elementsWithCoords.length === 0) return null;

  // 2. Prefer building ways/relations first, sorted by distance
  const buildingElements = elementsWithCoords
    .filter(({ el }) => (el.type === 'way' || el.type === 'relation') && el.tags?.building)
    .sort((a, b) => a.dist - b.dist);

  if (buildingElements.length > 0) return buildingElements[0].el.tags!.name;

  // 3. Otherwise pick the closest element with any name
  elementsWithCoords.sort((a, b) => a.dist - b.dist);
  return elementsWithCoords[0].el.tags!.name;
}
