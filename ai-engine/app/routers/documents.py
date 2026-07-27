import base64

from fastapi import APIRouter, Form, Header, HTTPException, UploadFile
from pydantic import BaseModel

from app.core.config import settings
from app.services import docx_autofill

router = APIRouter(prefix="/documents", tags=["documents"])


def verify_api_key(x_api_key: str) -> None:
    if x_api_key != settings.ai_engine_api_key:
        raise HTTPException(status_code=401, detail="유효하지 않은 API 키입니다.")


class AutoFillResponse(BaseModel):
    filled_document_base64: str
    filled_fields: dict[str, str]


@router.post("/auto-fill", response_model=AutoFillResponse)
async def auto_fill(
    file: UploadFile,
    company_text: str = Form(""),
    x_api_key: str = Header(default=""),
) -> AutoFillResponse:
    verify_api_key(x_api_key)

    content = await file.read()
    try:
        filled_bytes, filled_fields = docx_autofill.autofill(content, company_text)
    except Exception:
        raise HTTPException(status_code=422, detail="문서를 채우는 중 오류가 발생했습니다.")

    return AutoFillResponse(
        filled_document_base64=base64.b64encode(filled_bytes).decode(),
        filled_fields=filled_fields,
    )
