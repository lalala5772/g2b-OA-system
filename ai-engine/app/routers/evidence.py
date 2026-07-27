from fastapi import APIRouter, Header, HTTPException
from pydantic import BaseModel

from app.core.config import settings
from app.services import llm_client

router = APIRouter(prefix="/evidence", tags=["evidence"])

MAX_FILE_TEXT_CHARS = 800


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


class EvidenceFileInput(BaseModel):
    id: int
    filename: str
    text: str = ""


class MatchItemsRequest(BaseModel):
    items: list[RequiredItemSuggestion]
    evidence_files: list[EvidenceFileInput]


class ItemMatch(BaseModel):
    evidence_file_id: int | None = None
    reason: str | None = None


class MatchItemsResponse(BaseModel):
    matches: list[ItemMatch]


@router.post("/match-items", response_model=MatchItemsResponse)
def match_items(payload: MatchItemsRequest, x_api_key: str = Header(default="")) -> MatchItemsResponse:
    verify_api_key(x_api_key)

    if not payload.items:
        return MatchItemsResponse(matches=[])
    if not payload.evidence_files:
        return MatchItemsResponse(
            matches=[ItemMatch(reason="회사자료실 증빙서류 카테고리에 등록된 파일이 없습니다.") for _ in payload.items]
        )

    files_desc = "\n".join(
        f"id={f.id} | 파일명: {f.filename} | 내용: {f.text[:MAX_FILE_TEXT_CHARS]}" for f in payload.evidence_files
    )
    items_desc = "\n".join(
        f"{i}. {item.name}" + (f" ({item.description})" if item.description else "")
        for i, item in enumerate(payload.items)
    )

    result = llm_client.ask_json(
        system_prompt=(
            "너는 공고문이 요구하는 제출서류 항목과 회사가 실제로 보유한 파일 목록을 비교해 매칭하는 "
            "조달 실무 보조원이다. 각 항목마다, 회사 파일 중 그 서류 종류와 실제로 일치하는 파일이 있으면 "
            "해당 파일의 id를 골라라. 없거나 확신이 서지 않으면 null로 남겨라 — 추측해서 아무 파일이나 "
            "연결하지 마라. 파일명이나 주제가 비슷해도 서류 종류 자체가 다르면(예: 사업자등록증과 "
            "중소기업확인서, 재무제표와 납세증명서는 서로 다른 서류다) 매칭하지 마라. "
            "같은 파일 id를 서로 다른 두 항목에 중복 사용하지 마라 — 파일 하나는 최대 한 항목에만 대응한다. "
            "이모티콘은 사용하지 마라. 반드시 JSON 배열만 응답해라. 요청받은 항목 개수·순서와 정확히 "
            '같아야 한다: [{"evidence_file_id": 숫자 또는 null, "reason": "판단 이유 1문장"}]'
        ),
        user_prompt=f"제출서류 요구 항목:\n{items_desc}\n\n회사 보유 파일 목록:\n{files_desc}",
        max_tokens=2000,
    )

    valid_ids = {f.id for f in payload.evidence_files}
    return MatchItemsResponse(matches=_parse_matches(result, len(payload.items), valid_ids))


def _parse_matches(result, expected_count: int, valid_ids: set[int]) -> list[ItemMatch]:
    if not isinstance(result, list) or len(result) != expected_count:
        return [ItemMatch() for _ in range(expected_count)]

    matches: list[ItemMatch] = []
    used_ids: set[int] = set()
    for entry in result:
        if not isinstance(entry, dict):
            matches.append(ItemMatch())
            continue

        file_id = entry.get("evidence_file_id")
        try:
            file_id = int(file_id) if file_id is not None else None
        except (TypeError, ValueError):
            file_id = None

        # Never trust the model alone to honor "one file, one item" — a duplicate here would
        # crash zip generation with a duplicate entry name later, so enforce it in code too.
        if file_id is not None and (file_id not in valid_ids or file_id in used_ids):
            file_id = None
        if file_id is not None:
            used_ids.add(file_id)

        reason = entry.get("reason")
        matches.append(ItemMatch(evidence_file_id=file_id, reason=str(reason) if reason else None))
    return matches
