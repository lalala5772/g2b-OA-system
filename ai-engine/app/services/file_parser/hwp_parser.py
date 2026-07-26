"""Best-effort .hwp text extraction.

`pyhwp` (the `hwp5txt` converter) only supports the legacy binary .hwp format
and isn't a hard dependency of this service — installing it is optional
(see requirements.txt). `.hwpx` (the newer zip+XML format) isn't supported
here at all yet. In both unsupported cases we raise UnsupportedHwpError so
the caller can return the "convert to PDF and re-upload" fallback described
in docs/DESIGN.md.
"""

import subprocess
import tempfile
from pathlib import Path


class UnsupportedHwpError(Exception):
    pass


def extract_text(content: bytes) -> str:
    if content[:4] == b"PK\x03\x04":
        raise UnsupportedHwpError(".hwpx는 아직 지원되지 않습니다.")

    with tempfile.NamedTemporaryFile(suffix=".hwp", delete=False) as tmp:
        tmp.write(content)
        tmp_path = Path(tmp.name)

    try:
        result = subprocess.run(
            ["hwp5txt", str(tmp_path)],
            capture_output=True,
            text=True,
            timeout=30,
        )
        if result.returncode != 0 or not result.stdout.strip():
            raise UnsupportedHwpError("hwp5txt 변환에 실패했습니다.")
        return result.stdout
    except FileNotFoundError as exc:
        raise UnsupportedHwpError("hwp5txt(pyhwp)가 설치되어 있지 않습니다.") from exc
    finally:
        tmp_path.unlink(missing_ok=True)
