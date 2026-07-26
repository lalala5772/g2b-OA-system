from fastapi import APIRouter, Header, HTTPException
from pydantic import BaseModel

from app.core.config import settings
from app.services import bid_scanner, slack_notifier

router = APIRouter(prefix="/bids", tags=["bids"])


class ScanRequest(BaseModel):
    keywords: list[str]
    company_profile: str = ""
    eligibility_threshold: float = 0.6


class BidResult(BaseModel):
    external_bid_no: str | None = None
    title: str | None = None
    agency: str | None = None
    matched_keyword: str | None = None
    announce_date: str | None = None
    deadline: str | None = None
    url: str | None = None
    eligibility_score: float | None = None
    ai_judgement: str | None = None
    eligible: bool = False
    notification_status: str = "SKIPPED"
    notification_response_code: int | None = None


class ScanResponse(BaseModel):
    results: list[BidResult]


def verify_api_key(x_api_key: str) -> None:
    if x_api_key != settings.ai_engine_api_key:
        raise HTTPException(status_code=401, detail="유효하지 않은 API 키입니다.")


@router.post("/scan", response_model=ScanResponse)
def scan(payload: ScanRequest, x_api_key: str = Header(default="")) -> ScanResponse:
    verify_api_key(x_api_key)

    raw_results = bid_scanner.scan(payload.keywords, payload.company_profile, payload.eligibility_threshold)

    results = []
    for item in raw_results:
        notification_status, notification_code = "SKIPPED", None
        if item["eligible"]:
            message = (
                f"*[신규 적격 공고]* {item['title']}\n"
                f"발주기관: {item['agency']}\n"
                f"적격점수: {item['eligibility_score']}\n"
                f"{item['url'] or ''}"
            )
            notification_status, notification_code = slack_notifier.send(message)
        results.append(
            BidResult(
                **item,
                notification_status=notification_status,
                notification_response_code=notification_code,
            )
        )

    return ScanResponse(results=results)
