# g2b-OA-system

업무 자동화 플랫폼 — Spring Boot(핵심 서비스) + FastAPI(AI/문서 엔진) + React 하이브리드 구조.

전체 설계와 로드맵은 [`docs/DESIGN.md`](docs/DESIGN.md)를 참고하세요. 이 저장소는 설계서의 **Phase 1(로그인/대시보드/회사 자료실)**부터 순차적으로 구현합니다.

## 구성

| 디렉토리 | 역할 |
|---|---|
| `backend/` | Spring Boot API 서버 (인증, 도메인 트랜잭션, 오케스트레이션) |
| `ai-engine/` | FastAPI 내부 전용 서버 (문서 파싱, AI 연동) — Spring에서만 호출, 외부 미노출 |
| `frontend/` | React SPA |

## 로컬 실행

### 0. 사전 준비
```bash
cp backend/src/main/resources/application-local.yml.example backend/src/main/resources/application-local.yml
cp ai-engine/.env.example ai-engine/.env
cp frontend/.env.example frontend/.env
```
각 파일에 아래 값을 채워야 합니다(레포에는 절대 커밋되지 않음):
- Google OAuth Client ID/Secret ([Google Cloud Console](https://console.cloud.google.com/apis/credentials)에서 발급)
- `AI_ENGINE_API_KEY` — Spring↔FastAPI 내부 통신용 임의 문자열(직접 생성)

### 1. DB
```bash
docker compose up -d postgres
```

### 2. AI Engine (FastAPI)
```bash
cd ai-engine
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

### 3. Backend (Spring Boot)
```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

### 4. Frontend
```bash
cd frontend
npm install
npm run dev
```

## 현재 구현 범위 (Phase 1)
- Google OAuth2 로그인 → 자동 회원가입 → httpOnly 쿠키 세션
- 대시보드 허브(4개 기능 카드 + 실시간 요약)
- 회사 자료실(업로드 → 텍스트 자동 추출(docx/pdf, hwp는 베스트에포트) → 카테고리별 목록/삭제)

아이디어 제안 / 문서 자동 채우기 / 적격증빙 매칭 / 나라장터 자동화는 화면·API 골격만 있고 "Phase 2+ 제공 예정"으로 정직하게 표시됩니다.

## 보안 메모
- 실제 비밀정보(OAuth secret, API 키)는 `*.example` 템플릿만 커밋되고 실값은 로컬 `.env`/`application-local.yml`에만 존재합니다(`.gitignore` 처리).
- 회사 자료실 원본 파일은 현재 로컬 디스크(`backend/data/company-files`)에 저장됩니다. 배포 단계에서 `FileStorageService` 구현체를 S3로 교체할 예정입니다.
