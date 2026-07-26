from fastapi import FastAPI

from app.routers import bids, documents, embeddings, evidence, files

app = FastAPI(
    title="g2b-oa-ai-engine",
    description="Spring Boot 백엔드에서만 호출되는 내부 전용 AI/문서 처리 서버.",
    version="0.1.0",
)

app.include_router(files.router)
app.include_router(bids.router)
app.include_router(documents.router)
app.include_router(evidence.router)
app.include_router(embeddings.router)


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}
