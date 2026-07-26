import json

from fastapi import APIRouter, Form, Header, HTTPException, UploadFile
from fastapi.responses import Response
from pydantic import BaseModel

from app.core.config import settings
from app.services import docx_filler, llm_client

router = APIRouter(prefix="/documents", tags=["documents"])

DOCX_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"


def verify_api_key(x_api_key: str) -> None:
    if x_api_key != settings.ai_engine_api_key:
        raise HTTPException(status_code=401, detail="유효하지 않은 API 키입니다.")


@router.post("/fill")
async def fill(
    template: UploadFile,
    field_values: str = Form(...),
    x_api_key: str = Header(default=""),
) -> Response:
    verify_api_key(x_api_key)

    try:
        values = json.loads(field_values)
    except json.JSONDecodeError:
        raise HTTPException(status_code=400, detail="field_values가 올바른 JSON이 아닙니다.")

    content = await template.read()
    try:
        filled = docx_filler.fill(content, values)
    except Exception:
        raise HTTPException(status_code=422, detail="문서를 채우는 중 오류가 발생했습니다.")

    return Response(content=filled, media_type=DOCX_MEDIA_TYPE)


class ExtractFieldsRequest(BaseModel):
    company_text: str
    field_keys: list[str]


class ExtractFieldsResponse(BaseModel):
    fields: dict[str, str]


@router.post("/extract-fields", response_model=ExtractFieldsResponse)
def extract_fields(payload: ExtractFieldsRequest, x_api_key: str = Header(default="")) -> ExtractFieldsResponse:
    verify_api_key(x_api_key)

    if not payload.field_keys or not payload.company_text.strip():
        return ExtractFieldsResponse(fields={})

    result = llm_client.ask_json(
        system_prompt=(
            "너는 회사 소개 문서에서 특정 항목 값을 추출하는 어시스턴트다. "
            "반드시 JSON 객체만 응답해라. 요청받은 키만 포함하고, 찾을 수 없는 값은 빈 문자열로 남겨라."
        ),
        user_prompt=(
            f"회사 소개 텍스트:\n{payload.company_text}\n\n"
            f"다음 항목의 값을 추출해줘: {', '.join(payload.field_keys)}\n"
            f'JSON 형식 예시: {{"항목명": "값"}}'
        ),
        max_tokens=800,
    )
    if not isinstance(result, dict):
        return ExtractFieldsResponse(fields={})

    fields = {key: str(result.get(key, "")) for key in payload.field_keys}
    return ExtractFieldsResponse(fields=fields)
