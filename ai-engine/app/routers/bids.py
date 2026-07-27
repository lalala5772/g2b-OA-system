from fastapi import APIRouter, Header, HTTPException
from pydantic import BaseModel

from app.core.config import settings
from app.services import bid_scanner

router = APIRouter(prefix="/bids", tags=["bids"])


class ScanRequest(BaseModel):
    keywords: list[str]
    company_profile: str = ""
    eligibility_threshold: float = 0.6
    start_date: str | None = None
    end_date: str | None = None


class BidResult(BaseModel):
    external_bid_no: str | None = None
    title: str | None = None
    agency: str | None = None
    matched_keyword: str | None = None
    announce_date: str | None = None
    deadline: str | None = None
    url: str | None = None
    eligibility_score: float | None = None
    ai_summary: str | None = None
    ai_judgement: str | None = None
    eligible: bool = False


class ScanResponse(BaseModel):
    results: list[BidResult]
    fetched: int
    range_start: str
    range_end: str
    judged: int
    unjudged: int


def verify_api_key(x_api_key: str) -> None:
    if x_api_key != settings.ai_engine_api_key:
        raise HTTPException(status_code=401, detail="유효하지 않은 API 키입니다.")


@router.post("/scan", response_model=ScanResponse)
def scan(payload: ScanRequest, x_api_key: str = Header(default="")) -> ScanResponse:
    verify_api_key(x_api_key)

    outcome = bid_scanner.scan(
        payload.keywords,
        payload.company_profile,
        payload.eligibility_threshold,
        payload.start_date,
        payload.end_date,
    )
    return ScanResponse(
        results=[BidResult(**item) for item in outcome["results"]],
        fetched=outcome["fetched"],
        range_start=outcome["range_start"],
        range_end=outcome["range_end"],
        judged=outcome["judged"],
        unjudged=outcome["unjudged"],
    )
