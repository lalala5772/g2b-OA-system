from fastapi import APIRouter, Header, HTTPException
from pydantic import BaseModel

from app.core.config import settings
from app.services import llm_client

router = APIRouter(prefix="/ideas", tags=["ideas"])


def verify_api_key(x_api_key: str) -> None:
    if x_api_key != settings.ai_engine_api_key:
        raise HTTPException(status_code=401, detail="유효하지 않은 API 키입니다.")


class GenerateRequest(BaseModel):
    contest_text: str
    company_domain_texts: list[str]


class Idea(BaseModel):
    title: str
    content: str
    relevance_score: float | None = None


class GenerateResponse(BaseModel):
    ideas: list[Idea]


@router.post("/generate", response_model=GenerateResponse)
def generate(payload: GenerateRequest, x_api_key: str = Header(default="")) -> GenerateResponse:
    verify_api_key(x_api_key)

    company_profile = "\n\n".join(payload.company_domain_texts)
    result = llm_client.ask_json(
        system_prompt=(
            "너는 공모전/사업 제안 아이디어를 기획하는 어시스턴트다. "
            '반드시 JSON 배열만 응답해라: [{"title": "...", "content": "...", "relevance_score": 0~1}]'
        ),
        user_prompt=(
            f"회사 소개:\n{company_profile}\n\n"
            f"공모전 공고 내용:\n{payload.contest_text}\n\n"
            "회사 강점과 공모전 요건을 연결해 참여 가능한 아이디어를 3개 제안해줘."
        ),
        max_tokens=2000,
    )
    if not isinstance(result, list):
        return GenerateResponse(ideas=[])

    ideas = []
    for item in result:
        if not isinstance(item, dict) or "title" not in item or "content" not in item:
            continue
        score = item.get("relevance_score")
        try:
            score = float(score) if score is not None else None
        except (TypeError, ValueError):
            score = None
        ideas.append(Idea(title=str(item["title"]), content=str(item["content"]), relevance_score=score))

    return GenerateResponse(ideas=ideas)
