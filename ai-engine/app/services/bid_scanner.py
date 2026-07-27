"""나라장터 입찰공고정보서비스(BidPublicInfoService) 조회 + 적격 판단.

Endpoint/operation and field names were confirmed against public docs and
example code (see docs/DESIGN.md), not the full official Swagger — parsing
is defensive on purpose so an unexpected/renamed field degrades instead of
crashing the scan.

This API family has no keyword/title search parameter — only a date range
plus pagination (confirmed against a second, independent 나라장터 automation
tool's OpenAPI spec). So "search per keyword" happens client-side: pull the
whole date range (paginated, in parallel), then classify each notice by
which keyword(s) match its title.
"""

from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime, timedelta

import requests

from app.core.config import settings
from app.services import llm_client

OPERATION = "getBidPblancListInfoServcPPSSrch"
NUM_OF_ROWS = 999
MAX_PAGES = 5  # safety cap: up to ~5000 notices per scan regardless of range width
MAX_JUDGED = 50  # cap on how many matched notices get sent to Claude per scan


def _default_range() -> tuple[datetime, datetime]:
    end = datetime.now()
    begin = end - timedelta(days=7)
    return begin, end


def _resolve_range(start_date: str | None, end_date: str | None) -> tuple[datetime, datetime]:
    """start_date/end_date are "YYYY-MM-DD" from the frontend date picker. Falls back to the
    last 7 days when either is missing or unparseable."""
    if not start_date or not end_date:
        return _default_range()
    try:
        begin = datetime.strptime(start_date, "%Y-%m-%d")
        end = datetime.strptime(end_date, "%Y-%m-%d") + timedelta(hours=23, minutes=59)
        if begin > end:
            begin, end = end, begin
        return begin, end
    except ValueError:
        return _default_range()


def _fetch_page(begin: datetime, end: datetime, page_no: int) -> tuple[list[dict], int]:
    params = {
        # Use the raw (decoding) service key and let `requests` URL-encode it.
        # Appending the already-encoded key here double-encodes it and the
        # API responds with an auth error — a well-known gotcha for this API.
        "ServiceKey": settings.narajangteo_service_key,
        "type": "json",
        "inqryDiv": "1",
        "inqryBgnDt": begin.strftime("%Y%m%d%H%M"),
        "inqryEndDt": end.strftime("%Y%m%d%H%M"),
        "pageNo": str(page_no),
        "numOfRows": str(NUM_OF_ROWS),
    }
    try:
        response = requests.get(f"{settings.narajangteo_base_url}/{OPERATION}", params=params, timeout=20)
        response.raise_for_status()
        body = response.json().get("response", {}).get("body", {})
        items = body.get("items", [])
        if isinstance(items, dict):
            items = [items]
        total_count = int(body.get("totalCount") or len(items))
        return items, total_count
    except (requests.RequestException, ValueError, TypeError):
        return [], 0


def fetch_notices(start_date: str | None, end_date: str | None) -> list[dict]:
    if not settings.is_configured(settings.narajangteo_service_key):
        return []

    begin, end = _resolve_range(start_date, end_date)
    items, total_count = _fetch_page(begin, end, 1)
    if total_count <= len(items):
        return items

    total_pages = min(MAX_PAGES, -(-total_count // NUM_OF_ROWS))  # ceil division
    if total_pages <= 1:
        return items

    all_items = list(items)
    with ThreadPoolExecutor(max_workers=5) as executor:
        futures = [executor.submit(_fetch_page, begin, end, page) for page in range(2, total_pages + 1)]
        for future in as_completed(futures):
            page_items, _ = future.result()
            all_items.extend(page_items)
    return all_items


def _match_keyword(title: str, keywords: list[str]) -> str | None:
    for keyword in keywords:
        if keyword and keyword in title:
            return keyword
    return None


def _judge_eligibility(title: str, agency: str, company_profile: str) -> tuple[float | None, str | None, str | None]:
    if not company_profile.strip():
        return None, None, None
    result = llm_client.ask_json(
        system_prompt=(
            "너는 공공입찰 공고의 적격 여부를 판단하는 조달 실무 보조원이다. "
            "이모티콘이나 이모지는 절대 사용하지 마라. "
            '반드시 JSON만 응답해라: {"score": 0에서 1 사이 숫자, '
            '"summary": "공고 내용을 조달 실무자가 읽기 편하게 정리한 2~3문장", '
            '"reason": "이 회사가 왜 적합한지(또는 왜 적합하지 않은지) 1~2문장"}'
        ),
        user_prompt=(
            f"회사 소개:\n{company_profile}\n\n"
            f"공고명: {title}\n발주기관: {agency}\n\n"
            "이 회사가 이 입찰에 참여하기 적합한지 0~1 사이 점수로 평가하고, "
            "공고 요약과 추천 이유를 정리해줘."
        ),
        max_tokens=500,
    )
    if not isinstance(result, dict) or "score" not in result:
        return None, None, None
    try:
        return float(result["score"]), result.get("summary"), result.get("reason")
    except (TypeError, ValueError):
        return None, None, None


def _parse_date(value: str | None) -> str | None:
    if not value:
        return None
    digits = "".join(ch for ch in str(value) if ch.isdigit())
    if len(digits) < 8:
        return None
    return f"{digits[0:4]}-{digits[4:6]}-{digits[6:8]}"


def _build_result(
    item: dict, keyword: str, score: float | None, summary: str | None, reason: str | None, eligible: bool
) -> dict:
    return {
        "external_bid_no": item.get("bidNtceNo"),
        "title": item.get("bidNtceNm"),
        "agency": item.get("dminsttNm"),
        "matched_keyword": keyword,
        "announce_date": _parse_date(item.get("bidNtceDt")),
        "deadline": _parse_date(item.get("bidClseDt")),
        "url": item.get("ntceSpecDocUrl1") or item.get("bidNtceUrl"),
        "eligibility_score": score,
        "ai_summary": summary,
        "ai_judgement": reason,
        "eligible": eligible,
    }


def scan(
    keywords: list[str],
    company_profile: str,
    eligibility_threshold: float,
    start_date: str | None = None,
    end_date: str | None = None,
) -> dict:
    begin, end = _resolve_range(start_date, end_date)
    items = fetch_notices(start_date, end_date)

    matched: list[tuple[dict, str]] = []
    for item in items:
        keyword = _match_keyword(item.get("bidNtceNm", "") or "", keywords)
        if keyword:
            matched.append((item, keyword))

    to_judge = matched[:MAX_JUDGED]
    skipped = matched[MAX_JUDGED:]

    results = []
    judged_count = 0
    with ThreadPoolExecutor(max_workers=5) as executor:
        future_to_item = {
            executor.submit(
                _judge_eligibility, item.get("bidNtceNm", ""), item.get("dminsttNm", ""), company_profile
            ): (item, keyword)
            for item, keyword in to_judge
        }
        for future in as_completed(future_to_item):
            item, keyword = future_to_item[future]
            score, summary, reason = future.result()
            if score is not None:
                judged_count += 1
            eligible = (score or 0) >= eligibility_threshold
            results.append(_build_result(item, keyword, score, summary, reason, eligible))

    # Matched but not judged (MAX_JUDGED cap hit) are recorded as ineligible rather than dropped
    # silently, so a very wide date range doesn't just lose notices.
    for item, keyword in skipped:
        results.append(_build_result(item, keyword, None, None, None, False))

    return {
        "results": results,
        "fetched": len(items),
        "range_start": begin.date().isoformat(),
        "range_end": end.date().isoformat(),
        # A score is only ever None when a notice wasn't judged (call failed, no company
        # profile, or the MAX_JUDGED cap was hit) — a completed judgment always has a score,
        # even a low one. So matched-judged_count surfaces failures instead of hiding them
        # behind an empty eligible list, which is exactly what made a Claude billing outage
        # look identical to "this company's domain doesn't fit any of these keywords."
        "judged": judged_count,
        "unjudged": len(matched) - judged_count,
    }
