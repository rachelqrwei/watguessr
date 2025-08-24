#!/bin/bash

# Directory containing HEIC files
SRC_DIR="/Users/sooyeunleanne/Downloads/pics"
DST_DIR="/Users/sooyeunleanne/Downloads/pics_jpg"

# Create destination directory if it doesn't exist
mkdir -p "$DST_DIR"

# Loop from 1 to 42
for i in $(seq 1 44); do
    SRC_FILE="$SRC_DIR/$i.jpg"
    DST_FILE="$DST_DIR/$i.jpg"

    if [ -f "$SRC_FILE" ]; then
        echo "Converting $SRC_FILE -> $DST_FILE"
        magick convert "$SRC_FILE" "$DST_FILE"
    else
        echo "⚠️ File not found: $SRC_FILE"
    fi
done

echo "✅ Done converting HEIC files to JPEG!"
