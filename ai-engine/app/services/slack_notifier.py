import requests

from app.core.config import settings


def send(message: str) -> tuple[str, int | None]:
    """Returns (status, http_status_code) where status is SUCCESS/FAILED/SKIPPED."""
    if not settings.is_configured(settings.slack_webhook_url):
        return "SKIPPED", None
    try:
        response = requests.post(settings.slack_webhook_url, json={"text": message}, timeout=10)
        return ("SUCCESS" if response.ok else "FAILED"), response.status_code
    except requests.RequestException:
        return "FAILED", None
