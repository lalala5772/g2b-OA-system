from fastapi import APIRouter, Header, HTTPException
from pydantic import BaseModel

from app.core.config import settings
from app.services import bid_scanner

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


class ScanResponse(BaseModel):
    results: list[BidResult]


def verify_api_key(x_api_key: str) -> None:
    if x_api_key != settings.ai_engine_api_key:
        raise HTTPException(status_code=401, detail="유효하지 않은 API 키입니다.")


@router.post("/scan", response_model=ScanResponse)
def scan(payload: ScanRequest, x_api_key: str = Header(default="")) -> ScanResponse:
    verify_api_key(x_api_key)

    raw_results = bid_scanner.scan(payload.keywords, payload.company_profile, payload.eligibility_threshold)
    return ScanResponse(results=[BidResult(**item) for item in raw_results])
