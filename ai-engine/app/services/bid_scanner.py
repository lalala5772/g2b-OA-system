"""나라장터 입찰공고정보서비스(BidPublicInfoService) 조회 + 적격 판단.

Endpoint/operation and field names were confirmed against public docs and
example code (see docs/DESIGN.md), not the full official Swagger — parsing
is defensive on purpose so an unexpected/renamed field degrades instead of
crashing the scan.
"""

from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime, timedelta

import requests

from app.core.config import settings
from app.services import llm_client

OPERATION = "getBidPblancListInfoServcPPSSrch"


def fetch_recent_notices(hours: int = 24, num_of_rows: int = 100) -> list[dict]:
    if not settings.is_configured(settings.narajangteo_service_key):
        return []

    now = datetime.now()
    begin = now - timedelta(hours=hours)
    params = {
        # Use the raw (decoding) service key and let `requests` URL-encode it.
        # Appending the already-encoded key here double-encodes it and the
        # API responds with an auth error — a well-known gotcha for this API.
        "ServiceKey": settings.narajangteo_service_key,
        "type": "json",
        "inqryDiv": "1",
        "inqryBgnDt": begin.strftime("%Y%m%d%H%M"),
        "inqryEndDt": now.strftime("%Y%m%d%H%M"),
        "pageNo": "1",
        "numOfRows": str(num_of_rows),
    }

    try:
        response = requests.get(f"{settings.narajangteo_base_url}/{OPERATION}", params=params, timeout=15)
        response.raise_for_status()
        body = response.json().get("response", {}).get("body", {})
        items = body.get("items", [])
        if isinstance(items, dict):
            items = [items]
        return items
    except (requests.RequestException, ValueError):
        return []


def _match_keyword(title: str, keywords: list[str]) -> str | None:
    for keyword in keywords:
        if keyword and keyword in title:
            return keyword
    return None


def _judge_eligibility(title: str, agency: str, company_profile: str) -> tuple[float | None, str | None]:
    if not company_profile.strip():
        return None, None
    result = llm_client.ask_json(
        system_prompt=(
            "너는 공공입찰 공고의 적격 여부를 판단하는 어시스턴트다. "
            '반드시 JSON만 응답해라: {"score": 0에서 1 사이 숫자, "reason": "한두 문장 이유"}'
        ),
        user_prompt=(
            f"회사 소개:\n{company_profile}\n\n"
            f"공고명: {title}\n발주기관: {agency}\n\n"
            "이 회사가 이 입찰에 참여하기 적합한지 0~1 사이 점수로 평가해줘."
        ),
        max_tokens=300,
    )
    if not isinstance(result, dict) or "score" not in result:
        return None, None
    try:
        return float(result["score"]), result.get("reason")
    except (TypeError, ValueError):
        return None, None


def _parse_date(value: str | None) -> str | None:
    if not value:
        return None
    digits = "".join(ch for ch in str(value) if ch.isdigit())
    if len(digits) < 8:
        return None
    return f"{digits[0:4]}-{digits[4:6]}-{digits[6:8]}"


def scan(keywords: list[str], company_profile: str, eligibility_threshold: float) -> list[dict]:
    items = fetch_recent_notices()
    matched = []
    for item in items:
        keyword = _match_keyword(item.get("bidNtceNm", "") or "", keywords)
        if keyword:
            matched.append((item, keyword))

    results = []
    with ThreadPoolExecutor(max_workers=3) as executor:
        future_to_item = {
            executor.submit(
                _judge_eligibility, item.get("bidNtceNm", ""), item.get("dminsttNm", ""), company_profile
            ): (item, keyword)
            for item, keyword in matched
        }
        for future in as_completed(future_to_item):
            item, keyword = future_to_item[future]
            score, reason = future.result()
            eligible = (score or 0) >= eligibility_threshold
            results.append(
                {
                    "external_bid_no": item.get("bidNtceNo"),
                    "title": item.get("bidNtceNm"),
                    "agency": item.get("dminsttNm"),
                    "matched_keyword": keyword,
                    "announce_date": _parse_date(item.get("bidNtceDt")),
                    "deadline": _parse_date(item.get("bidClseDt")),
                    "url": item.get("ntceSpecDocUrl1") or item.get("bidNtceUrl"),
                    "eligibility_score": score,
                    "ai_judgement": reason,
                    "eligible": eligible,
                }
            )
    return results
