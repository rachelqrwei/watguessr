import requests
import time

# List of coordinates (lat, lon, description, floor)
coords = [
    (43.4739335, -80.5404674, "bus station", "Ground"),
    (43.4735050, -80.5416167, "ion station", "Ground"),
    (43.4729449, -80.5415279, "dc", "Ground"),
    (43.4726622, -80.5402870, "e2 bridge", "Ground"),
    (43.4719354, -80.5418893, "side of earth science building", "Ground"),
    (43.4717055, -80.5433749, "egg fountain", "Ground"),
    (43.4714960, -80.5434403, "outside qnc", "Ground"),
    (43.4717023, -80.5444394, "mc", "Ground"),
    (43.4725554, -80.5452739, "gazebo thing", "Ground"),
    (43.4729809, -80.5460937, "outside health building", "Ground"),
    (43.4726821, -80.5468795, "health building", "Ground"),
    (43.4723717, -80.5477053, "university club building", "Ground"),
    (43.4721016, -80.5481305, "buildings in distance", "Ground"),
    (43.4715524, -80.5474395, "outside", "Ground"),
    (43.4715524, -80.5474395, "outside", "Ground"),
    (43.4712186, -80.5461061, "idk what building that is", "Ground"),
    (43.4709556, -80.5448645, "qnc", "Ground"),
    (43.4702565, -80.5445883, "bridge + st. jerome’s building", "Ground"),
    (43.4702565, -80.5445883, "bridge + st. jerome’s building", "Ground"),
    (43.4703721, -80.5445386, "stc", "Ground"),
    (43.4702589, -80.5435764, "a parking lot building that i did not even know existed", "Ground"),
    (43.4701638, -80.5426903, "stc/b2 greenhouse", "Ground"),
    (43.4699777, -80.5419818, "dp", "Ground"),
    (43.4699777, -80.5419818, "dp", "Ground"),
    (43.4695088, -80.5404902, "w store/south campus hall", "Ground"),
    (43.4697528, -80.5406987, "graduate house", "Ground"),
    (43.4701855, -80.5408375, "inside rch (3rd/2nd floor)", "2nd Floor"),
    (43.4701855, -80.5408375, "inside rch (2nd floor)", "2nd Floor"),
    (43.4706305, -80.5409572, "inside e2/physics i think?????", "1st Floor"),
    (43.4705407, -80.5392178, "whatever this structure is", "Ground"),
    (43.4717617, -80.5396584, "e7", "Ground"),
    (43.4717617, -80.5396584, "e7", "Ground"),
    (43.4717617, -80.5396584, "e7 + ion", "Ground"),
    (43.4720592, -80.5397167, "e7", "Ground"),
    (43.4731050, -80.5369245, "inside hi yogurt lol", "Ground"),
    (43.4730237, -80.5396148, "inside e7 (1st floor)", "1st Floor"),
    (43.4730921, -80.5398294, "inside e7 (2nd floor)", "2nd Floor"),
    (43.4719159, -80.5421280, "inside earth sci (1st floor)", "1st Floor"),
    (43.4717135, -80.5421381, "inside earth sci (1st floor)", "1st Floor"),
    (43.4717135, -80.5421381, "inside earth sci (1st floor)", "1st Floor"),
    (43.4717135, -80.5421381, "inside earth sci (1st floor)", "1st Floor"),
    (43.4711412, -80.5427221, "inside earth sci (1st floor)", "1st Floor"),
    (43.4711412, -80.5427221, "inside earth sci (1st floor)", "1st Floor"),
    (43.4711412, -80.5427221, "inside earth sci (1st floor)", "1st Floor"),
]

OVERPASS_URL = "http://overpass-api.de/api/interpreter"

def get_building_name(lat, lon, radius=25):
    """
    Query OSM Overpass API for nearby building names.
    """
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
    return None


if __name__ == "__main__":
    lines = ["#!/bin/bash\n"]
    for i, (lat, lon, desc, floor) in enumerate(coords, start=1):
        print(f"Looking up building at {desc} ({lat}, {lon}) floor={floor}...")
        name = get_building_name(lat, lon)
        if not name:
            name = ''  # fallback if OSM didn’t find anything

        # Escape quotes
        safe_name = name.replace('"', '\\"')

        # Image path increments
        img_path = f"/Users/sooyeunleanne/Downloads/pics/{i}.heic"

        cmd = f"""python scripts/upload_scenes.py \\
    --aws-profile watguessr-upload \\
    --bucket watguessr \\
    --region us-east-2 \\
    --building-name "{safe_name}" \\
    --floor "{floor}" \\
    --lon {lon} \\
    --lat {lat} \\
    {img_path}\n"""
        lines.append(cmd)
        time.sleep(2)  # avoid hammering API

    # Save bash script
    with open("../run_uploads.sh", "w", encoding="utf-8") as f:
        f.writelines(lines)

    print("\n✅ Done! Generated run_uploads.sh")