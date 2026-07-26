# g2b-OA-system

업무 자동화 플랫폼 — Spring Boot(핵심 서비스) + FastAPI(AI/문서 엔진) + React 하이브리드 구조.

전체 설계와 로드맵은 [`docs/DESIGN.md`](docs/DESIGN.md)를 참고하세요. Phase 1(로그인/대시보드/회사 자료실)부터 Phase 5(적격증빙자료 매칭)까지 구현되어 있습니다.

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
각 파일에 아래 값을 채워야 합니다(레포에는 절대 커밋되지 않으며, `*.example`만 커밋됩니다):

**`backend/src/main/resources/application-local.yml`**
- `spring.security.oauth2.client.registration.google.client-id` / `client-secret` — [Google Cloud Console](https://console.cloud.google.com/apis/credentials)에서 발급
- `app.jwt.secret` — 32바이트 이상 임의 문자열
- `ai-engine.api-key` — 아래 `AI_ENGINE_API_KEY`와 반드시 동일한 값(Spring↔FastAPI 내부 통신용)

**`ai-engine/.env`**
- `AI_ENGINE_API_KEY` — 위와 동일한 값
- `CLAUDE_API_KEY` / `CLAUDE_MODEL` — 나라장터 적격판단·문서 필드 자동추출·아이디어 제안·증빙 요건 추출에 사용
- `NARAJANGTEO_SERVICE_KEY` — 나라장터 Open API **디코딩키**(인코딩키 아님 — 인코딩키를 넣으면 이중 인코딩되어 인증 오류가 납니다)
- `SLACK_WEBHOOK_URL` — 비워두면 알림 전송을 건너뛰고 로그만 남깁니다(크래시 없음)
- `EMBEDDING_MODEL` — 기본값 그대로 두면 됩니다(로컬 sentence-transformers, API 키 불필요)

Claude/나라장터/Slack 키가 없어도 앱은 정상적으로 뜨고, 해당 기능만 "결과 없음"으로 정직하게 응답합니다(크래시하지 않음).

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
> 첫 회사 자료실 "증빙서류" 업로드 시 로컬 임베딩 모델을 최초 1회 다운로드합니다(수십 초 소요, 이후 캐시됨).

### 3. Backend (Spring Boot)
```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```
나라장터 자동 스캔은 매일 `app.bid.scan-cron`(기본 09:00, `BID_SCAN_CRON`으로 변경 가능) 시각에 자동 실행됩니다. 즉시 테스트하려면 나라장터 페이지의 "지금 스캔 실행" 버튼을 사용하세요. 키워드는 `BID_KEYWORDS_SEED`(콤마 구분, 최초 기동 시 1회 시드) 환경변수나 나라장터 페이지에서 직접 추가할 수 있습니다.

### 4. Frontend
```bash
cd frontend
npm install
npm run dev
```

## 현재 구현 범위
- **Phase 1**: Google OAuth2 로그인, 대시보드 허브, 회사 자료실(업로드/파싱/목록/삭제)
- **Phase 2**: 나라장터 자동화 — 키워드 관리, 매일 자동 스캔(`@Scheduled`) + 수동 실행, AI 적격판단, Slack 알림
- **Phase 3**: 문서 자동 채우기 — Word 템플릿 업로드, 회사 고정정보 자동추출 + 나머지 항목 수동입력, 다운로드
- **Phase 4**: 아이디어 제안 — 공모전 파일 업로드 → 자료실 도메인소개 자료 자동 참조 → 아이디어 생성
- **Phase 5**: 적격증빙자료 매칭 — 공고문 업로드 → 제출서류 추출 → 자료실 증빙자료와 임베딩 매칭 → ZIP 생성

## 나라장터 API 관련 주의사항
`getBidPblancListInfoServcPPSSrch` 오퍼레이션의 요청/응답 필드는 공개된 예제와 문서를 참고해 구현했으며, 공식 Swagger 전체를 확인하지는 못했습니다(`ai-engine/app/services/bid_scanner.py`). 실제 키로 처음 실행했을 때 응답 필드명이 다르면(예: 날짜 필드, URL 필드) `_parse_date` 및 `item.get(...)` 부분을 응답 형태에 맞게 조정해주세요. 파싱은 방어적으로 되어 있어 필드가 안 맞아도 크래시 없이 빈 값으로 처리됩니다.

## 설계서 대비 구현 편차
Phase 2~5를 구현하며 설계서(`docs/DESIGN.md`) 원안에서 몇 가지를 실용적으로 조정했습니다 — 상세 이유는 문서 하단 참고:
- 알림 채널: Google Chat → **Slack** Incoming Webhook
- 임베딩: Claude API(임베딩 미제공) → **로컬 sentence-transformers**
- 벡터 검색: pgvector/FAISS → **단순 코사인 유사도**(회사 자료 규모에서는 충분)

## 보안 메모
- 실제 비밀정보(OAuth secret, API 키)는 `*.example` 템플릿만 커밋되고 실값은 로컬 `.env`/`application-local.yml`에만 존재합니다(`.gitignore` 처리).
- 회사 자료실 원본 파일은 현재 로컬 디스크(`backend/data/company-files`)에 저장됩니다. 배포 단계에서 `FileStorageService` 구현체를 S3로 교체할 예정입니다.
