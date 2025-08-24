#!/usr/bin/env python3
"""
Bulk scene uploader for WatGuessr.

HOW TO RUN:
1. Install AWS CLI, brew install awscli (if not already installed) run these commands in the terminal

2. aws configure --profile watguessr-upload
set username and password to the one i sent yall

3. set up environemnt variables
export SPRING_DATASOURCE_URL="jdbc:postgresql://db-postgresql-nyc3-28009-do-user-24874859-0.j.db.ondigitalocean.com:25060/watguessr" OR 5432 DEPENDING ON WHICH U USE
export SPRING_DATASOURCE_USERNAME="watuser"
export SPRING_DATASOURCE_PASSWORD="goon"
export AWS_REGION="us-east-2"

4. Install dependencies
    cd into your watguessr directory
    python3 -m venv .venv
    source .venv/bin/activate
    pip install -r scripts/requirements.txt

5. Run the script, fill out the fields for your thingy
    python scripts/upload_scenes.py \
    --aws-profile watguessr-upload \
    --bucket watguessr \
    --region us-east-2 \
    --building-name "" \
    --floor "Ground" \
    --lon -80.5498286537569 \
    --lat 43.4709910994781 \
    /Users/rache/Downloads/watguessr-scene-images/south1.jpg
    find the absolute path on your mac, you can get it by holding option button in finder


What it does for each input image:
- Generates a UUID for the scene
- Builds S3 object key as "<prefix>/<first8_of_uuid>.jpg"
- Compresses the image to JPEG (quality/resize configurable)
- Uploads to S3 using the provided AWS profile
- Inserts a row into the Postgres "scene" table with the S3 key and metadata

Reads DB connection from env by default (SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD)
"""

from __future__ import annotations

import argparse
import csv
import io
import os
import re
import sys
import uuid
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, List, Optional, Tuple

import boto3
import psycopg
from psycopg import sql
from PIL import Image, ImageOps


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Upload scenes to S3 and insert DB rows")

    # AWS
    parser.add_argument("files", nargs="+", help="Image files or directories to upload")
    parser.add_argument("--aws-profile", default=os.getenv("AWS_PROFILE", "watguessr-upload"))
    parser.add_argument("--bucket", default=os.getenv("AWS_S3_BUCKET_NAME"), help="S3 bucket name")
    parser.add_argument("--region", default=os.getenv("AWS_REGION", "us-east-2"))
    parser.add_argument("--prefix", default="", help="S3 key prefix (folder). Default: none (bucket root)")
    parser.add_argument(
        "--db-store-full-key",
        action="store_true",
        help="Store full S3 key (including prefix) in DB image field. Default stores filename only.",
    )
    parser.add_argument("--storage-class", default=None, help="Optional S3 storage class, e.g. STANDARD_IA")

    # Scene metadata (applied to all files)
    building = parser.add_mutually_exclusive_group(required=False)
    building.add_argument("--building-id", help="Building UUID")
    building.add_argument("--building-name", help="Building name to look up")
    parser.add_argument("--floor", required=True, help="Floor label, e.g. '1', '2', 'Ground', 'Basement'")
    parser.add_argument("--lon", type=float, required=True, help="Longitude")
    parser.add_argument("--lat", type=float, required=True, help="Latitude")

    # Image processing
    parser.add_argument("--quality", type=int, default=75, help="JPEG quality 1-100 (default: 75)")
    parser.add_argument("--max-dimension", type=int, default=1920, help="Max width/height to downscale to (default: 1920)")

    # Database (defaults from env)
    parser.add_argument("--db-url", default=os.getenv("SPRING_DATASOURCE_URL"))
    parser.add_argument("--db-user", default=os.getenv("SPRING_DATASOURCE_USERNAME"))
    parser.add_argument("--db-pass", default=os.getenv("SPRING_DATASOURCE_PASSWORD"))
    parser.add_argument("--db-schema", default=os.getenv("SPRING_JPA_DEFAULT_SCHEMA", "watguessr"))

    # Output
    parser.add_argument("--out-csv", default="scene_upload_results.csv")

    args = parser.parse_args()

    if not args.bucket:
        parser.error("--bucket is required (or set AWS_S3_BUCKET_NAME)")
    if not args.db_url or not args.db_user or not args.db_pass:
        parser.error("Database flags/env missing. Provide --db-url, --db-user, --db-pass or set env SPRING_DATASOURCE_URL/USERNAME/PASSWORD")
    return args


def iter_image_files(paths: Iterable[str]) -> List[Path]:
    image_exts = {".jpg", ".jpeg", ".png", ".webp", ".bmp", ".tif", ".tiff"}
    files: List[Path] = []
    for p in paths:
        path = Path(p)
        if path.is_dir():
            for child in path.rglob("*"):
                if child.is_file() and child.suffix.lower() in image_exts:
                    files.append(child)
        elif path.is_file() and path.suffix.lower() in image_exts:
            files.append(path)
        else:
            print(f"Skipping non-image path: {path}", file=sys.stderr)
    return files


def compress_image_to_jpeg_bytes(src: Path, quality: int, max_dimension: Optional[int]) -> Tuple[bytes, str]:
    """Return (bytes, content_type) for the compressed JPEG image."""
    with Image.open(src) as im:
        im = ImageOps.exif_transpose(im)
        if max_dimension:
            im.thumbnail((max_dimension, max_dimension))
        if im.mode not in ("RGB", "L"):
            im = im.convert("RGB")
        out = io.BytesIO()
        im.save(out, format="JPEG", quality=quality, optimize=True, progressive=True)
        return out.getvalue(), "image/jpeg"


@dataclass
class DbConfig:
    host: str
    port: int
    dbname: str
    user: str
    password: str
    schema: str


def parse_jdbc_url(jdbc_url: str) -> Tuple[str, int, str]:
    """Parse JDBC URL like jdbc:postgresql://host:5432/dbname and return (host, port, dbname)."""
    m = re.match(r"jdbc:postgresql://([^/:]+)(?::(\d+))?/([^?]+)", jdbc_url)
    if not m:
        raise ValueError(f"Unsupported JDBC URL: {jdbc_url}")
    host = m.group(1)
    port = int(m.group(2) or 5432)
    dbname = m.group(3)
    return host, port, dbname


def get_db_config(args: argparse.Namespace) -> DbConfig:
    host, port, dbname = parse_jdbc_url(args.db_url)
    return DbConfig(
        host=host,
        port=port,
        dbname=dbname,
        user=args.db_user,
        password=args.db_pass,
        schema=args.db_schema,
    )


def connect_db(cfg: DbConfig):
    conn = psycopg.connect(host=cfg.host, port=cfg.port, dbname=cfg.dbname, user=cfg.user, password=cfg.password)
    conn.autocommit = False
    with conn.cursor() as cur:
        # Use identifier composition; parameters cannot be used for identifiers
        cur.execute(
            sql.SQL("SET search_path TO {};").format(sql.Identifier(cfg.schema))
        )
    return conn


def ensure_building_id(conn, building_id: Optional[str], building_name: Optional[str]) -> Optional[uuid.UUID]:
    if building_id:
        return uuid.UUID(building_id)
    if building_name:
        with conn.cursor() as cur:
            cur.execute("SELECT id FROM building WHERE name = %s LIMIT 1;", (building_name,))
            row = cur.fetchone()
            if not row:
                raise ValueError(f"Building not found by name: {building_name}")
            return row[0]
    # Neither provided → insert NULL in DB
    return None


def create_s3_client(profile: str, region: str):
    session = boto3.Session(profile_name=profile, region_name=region)
    return session.client("s3")


def upload_one(
    s3,
    bucket: str,
    storage_class: Optional[str],
    prefix: str,
    img_path: Path,
    quality: int,
    max_dimension: Optional[int],
    conn,
    building_id: Optional[uuid.UUID],
    floor: str,
    lon: float,
    lat: float,
    db_store_full_key: bool,
) -> Tuple[uuid.UUID, str]:
    scene_id = uuid.uuid4()
    filename = f"{scene_id.hex[:8]}.jpg"
    key = f"{prefix}/{filename}" if prefix else filename

    body, content_type = compress_image_to_jpeg_bytes(img_path, quality=quality, max_dimension=max_dimension)

    put_kwargs = {"Bucket": bucket, "Key": key, "Body": body, "ContentType": content_type}
    if storage_class:
        put_kwargs["StorageClass"] = storage_class
    s3.put_object(**put_kwargs)

    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO scene (id, image, location_x, location_y, floor, building_id)
            VALUES (%s, %s, %s, %s, %s, %s)
            """,
            (scene_id, (key if db_store_full_key else filename), float(lon), float(lat), floor, building_id),
        )

    return scene_id, key


def main() -> int:
    args = parse_args()

    files = iter_image_files(args.files)
    if not files:
        print("No image files found.", file=sys.stderr)
        return 1

    db_cfg = get_db_config(args)
    conn = connect_db(db_cfg)
    s3 = create_s3_client(args.aws_profile, args.region)

    try:
        building_id = ensure_building_id(conn, args.building_id, args.building_name)

        out_rows = [("original_path", "scene_uuid", "s3_key")]
        for img in files:
            scene_uuid, key = upload_one(
                s3=s3,
                bucket=args.bucket,
                storage_class=args.storage_class,
                prefix=args.prefix,
                img_path=img,
                quality=args.quality,
                max_dimension=args.max_dimension,
                conn=conn,
                building_id=building_id,
                floor=args.floor,
                lon=args.lon,
                lat=args.lat,
                db_store_full_key=args.db_store_full_key,
            )
            print(f"Uploaded {img} -> s3://{args.bucket}/{key} (scene {scene_uuid})")
            out_rows.append((str(img), str(scene_uuid), key))

        conn.commit()

        with open(args.out_csv, "w", newline="") as f:
            writer = csv.writer(f)
            writer.writerows(out_rows)
        print(f"Wrote {args.out_csv}")
    except Exception as e:
        conn.rollback()
        print(f"ERROR: {e}", file=sys.stderr)
        return 2
    finally:
        conn.close()

    return 0


if __name__ == "__main__":
    raise SystemExit(main())


