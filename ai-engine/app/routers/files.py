from fastapi import APIRouter, File, Header, UploadFile
from pydantic import BaseModel

from app.core.security import verify_api_key
from app.services.file_parser import docx_parser, hwp_parser, pdf_parser

router = APIRouter(prefix="/files", tags=["files"])

FALLBACK_MESSAGE = "이 형식은 자동으로 읽을 수 없습니다. PDF로 변환 후 다시 업로드해주세요."


class ParseResponse(BaseModel):
    status: str
    extracted_text: str | None = None
    message: str | None = None


@router.post("/parse", response_model=ParseResponse, dependencies=[])
async def parse_file(file: UploadFile = File(...), x_api_key: str = Header(default="")) -> ParseResponse:
    verify_api_key(x_api_key)

    filename = file.filename or ""
    extension = filename.rsplit(".", 1)[-1].lower() if "." in filename else ""
    content = await file.read()

    try:
        if extension == "docx":
            text = docx_parser.extract_text(content)
        elif extension == "pdf":
            text = pdf_parser.extract_text(content)
        elif extension in ("hwp", "hwpx"):
            text = hwp_parser.extract_text(content)
        else:
            return ParseResponse(status="failed", message=f"지원하지 않는 확장자입니다: .{extension}")
    except hwp_parser.UnsupportedHwpError as exc:
        return ParseResponse(status="failed", message=f"{exc} {FALLBACK_MESSAGE}")
    except Exception:
        return ParseResponse(status="failed", message="파일을 분석하는 중 오류가 발생했습니다.")

    if not text.strip():
        return ParseResponse(status="failed", message="파일에서 텍스트를 추출하지 못했습니다.")

    return ParseResponse(status="success", extracted_text=text)
