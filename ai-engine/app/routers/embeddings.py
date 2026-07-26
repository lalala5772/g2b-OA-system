from fastapi import APIRouter, Header, HTTPException
from pydantic import BaseModel

from app.core.config import settings
from app.services import embedding_service

router = APIRouter(prefix="/embeddings", tags=["embeddings"])


def verify_api_key(x_api_key: str) -> None:
    if x_api_key != settings.ai_engine_api_key:
        raise HTTPException(status_code=401, detail="유효하지 않은 API 키입니다.")


class EncodeRequest(BaseModel):
    text: str


class EncodeResponse(BaseModel):
    embedding: list[float]


@router.post("/encode", response_model=EncodeResponse)
def encode(payload: EncodeRequest, x_api_key: str = Header(default="")) -> EncodeResponse:
    verify_api_key(x_api_key)
    return EncodeResponse(embedding=embedding_service.encode(payload.text))
