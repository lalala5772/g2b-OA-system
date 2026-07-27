"""Thin wrapper around the Anthropic Messages API.

Every caller here wants structured JSON back, so `ask_json` centralizes the
"ask for JSON, tolerate a bit of prose around it" parsing instead of each
router re-implementing it. Returns None (never raises) when the API key
isn't configured or the response can't be parsed, so callers can degrade
gracefully instead of 500ing.
"""

import json
import logging
import re
from functools import lru_cache

import anthropic
from anthropic import Anthropic

from app.core.config import settings

logger = logging.getLogger(__name__)


@lru_cache(maxsize=1)
def _client() -> Anthropic | None:
    if not settings.is_configured(settings.claude_api_key):
        return None
    return Anthropic(api_key=settings.claude_api_key)


def is_available() -> bool:
    return _client() is not None


def ask_json(system_prompt: str, user_prompt: str, max_tokens: int = 1536):
    client = _client()
    if client is None:
        return None

    try:
        response = client.messages.create(
            model=settings.claude_model,
            max_tokens=max_tokens,
            system=system_prompt,
            messages=[{"role": "user", "content": user_prompt}],
        )
    except anthropic.APIError as exc:
        # A single failed call (rate limit, low credit, transient outage) shouldn't
        # take down a whole batch of parallel judgment calls — degrade to "unjudged".
        # Still log it: a silently-swallowed failure looks identical to "AI judged
        # this ineligible" from the caller's side, which is hard to debug blind.
        logger.warning("Claude API call failed, degrading to unjudged: %s", exc)
        return None
    text = "".join(block.text for block in response.content if block.type == "text")
    return _extract_json(text)


def _extract_json(text: str):
    match = re.search(r"\{.*\}|\[.*\]", text, re.DOTALL)
    if not match:
        return None
    try:
        return json.loads(match.group(0))
    except json.JSONDecodeError:
        return None
