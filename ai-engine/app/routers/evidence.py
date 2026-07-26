from fastapi import APIRouter, Header, HTTPException
from pydantic import BaseModel

from app.core.config import settings
from app.services import llm_client

router = APIRouter(prefix="/evidence", tags=["evidence"])


def verify_api_key(x_api_key: str) -> None:
    if x_api_key != settings.ai_engine_api_key:
        raise HTTPException(status_code=401, detail="유효하지 않은 API 키입니다.")


class ExtractRequirementsRequest(BaseModel):
    requirement_text: str


class RequiredItemSuggestion(BaseModel):
    name: str
    description: str = ""


class ExtractRequirementsResponse(BaseModel):
    items: list[RequiredItemSuggestion]


@router.post("/extract-requirements", response_model=ExtractRequirementsResponse)
def extract_requirements(
    payload: ExtractRequirementsRequest, x_api_key: str = Header(default="")
) -> ExtractRequirementsResponse:
    verify_api_key(x_api_key)

    result = llm_client.ask_json(
        system_prompt=(
            "너는 공공입찰/공모전 공고문에서 제출서류 목록을 추출하는 어시스턴트다. "
            '반드시 JSON 배열만 응답해라: [{"name": "사업자등록증", "description": "..."}]'
        ),
        user_prompt=f"다음 공고문에서 제출해야 하는 서류 목록을 추출해줘:\n\n{payload.requirement_text}",
        max_tokens=1500,
    )
    if not isinstance(result, list):
        return ExtractRequirementsResponse(items=[])

    items = []
    for item in result:
        if not isinstance(item, dict) or "name" not in item:
            continue
        items.append(RequiredItemSuggestion(name=str(item["name"]), description=str(item.get("description", ""))))

    return ExtractRequirementsResponse(items=items)
