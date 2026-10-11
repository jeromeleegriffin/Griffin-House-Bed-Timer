#!/usr/bin/env python3
"""Extract the exact embedded PNG from the approved Bed Timer website HTML."""
import base64
import hashlib
import pathlib
import re
import struct

root = pathlib.Path(__file__).resolve().parents[2]
html = (root / "android/app/src/main/assets/bed-times.html").read_text(encoding="utf-8")
match = re.search(r'<link\s+rel="icon"\s+type="image/png"\s+href="data:image/png;base64,([^"]+)"', html)
if not match:
    raise SystemExit("Original website icon missing; refusing to substitute artwork")
data = base64.b64decode(match.group(1), validate=True)
if data[:8] != b"\x89PNG\r\n\x1a\n":
    raise SystemExit("Embedded icon is not a PNG")
width, height = struct.unpack(">II", data[16:24])
if (width, height) != (192, 192):
    raise SystemExit(f"Unexpected original icon dimensions: {width}x{height}")
dest = root / "android/app/src/main/res/drawable-nodpi/bed_timer_original.png"
dest.parent.mkdir(parents=True, exist_ok=True)
dest.write_bytes(data)
print(f"Extracted original icon: {width}x{height}, SHA-256 {hashlib.sha256(data).hexdigest()}")
