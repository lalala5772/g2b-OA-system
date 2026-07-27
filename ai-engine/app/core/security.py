import hmac

from fastapi import HTTPException

from app.core.config import settings


def verify_api_key(x_api_key: str) -> None:
    """Shared by every router — this API is internal-only (Spring calls it over
    a private network), but constant-time comparison costs nothing and rules
    out a timing side-channel."""
    if not hmac.compare_digest(x_api_key, settings.ai_engine_api_key):
        raise HTTPException(status_code=401, detail="유효하지 않은 API 키입니다.")
