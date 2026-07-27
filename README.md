# g2b-OA-system

업무 자동화 플랫폼 — Spring Boot(핵심 서비스) + FastAPI(AI/문서 엔진) + React 하이브리드 구조.

전체 설계와 로드맵은 [`docs/DESIGN.md`](docs/DESIGN.md)를 참고하세요. Phase 1(로그인/대시보드/회사 자료실)부터 Phase 5(적격증빙자료 매칭)까지 구현되어 있습니다(아이디어 제안 기능은 사용자 요청으로 범위에서 제외).

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
- `CLAUDE_API_KEY` / `CLAUDE_MODEL` — 나라장터 적격판단·문서 필드 자동추출·증빙 요건 추출에 사용
- `NARAJANGTEO_SERVICE_KEY` — 나라장터 Open API **디코딩키**(인코딩키 아님 — 인코딩키를 넣으면 이중 인코딩되어 인증 오류가 납니다)

Claude/나라장터 키가 없어도 앱은 정상적으로 뜨고, 해당 기능만 "결과 없음"으로 정직하게 응답합니다(크래시하지 않음).

### 한 번에 실행 (권장)
사전 준비(위 0단계)만 끝냈다면, 아래 명령 하나로 Postgres·AI Engine·Backend·Frontend를 전부 백그라운드로 띄웁니다:
```bash
./scripts/dev.sh
```
최초 실행 시 `ai-engine/.venv`와 `frontend/node_modules`가 없으면 자동으로 만들어줍니다. `Ctrl+C`를 누르면 4개 프로세스와 Postgres 컨테이너까지 한 번에 정리됩니다. 개별 로그는 `logs/backend.log`, `logs/ai-engine.log`, `logs/frontend.log`에서 확인하세요(`tail -f logs/backend.log`).

각 서비스를 따로 띄우거나 디버깅하고 싶다면 아래 1~4단계를 그대로 따라가면 됩니다.

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
나라장터 자동 스캔은 매일 `app.bid.scan-cron`(기본 10:00, `BID_SCAN_CRON`으로 변경 가능) 시각에 자동 실행됩니다. 즉시 테스트하려면 나라장터 페이지의 "지금 스캔 실행" 버튼을 사용하세요. 키워드는 `BID_KEYWORDS_SEED`(콤마 구분, 최초 기동 시 1회 시드) 환경변수나 나라장터 페이지에서 직접 추가할 수 있습니다.

### 4. Frontend
```bash
cd frontend
npm install
npm run dev
```

## 현재 구현 범위
- **Phase 1**: Google OAuth2 로그인, 대시보드 허브, 회사 자료실(업로드/파싱/목록/삭제) — 업로드는 왼쪽에서 선택한 카테고리로만 들어갑니다(목록 필터와 업로드 대상이 동일한 상태를 공유)
- **Phase 2**: 나라장터 자동화 — 키워드 최대 15개 등록, 자동 스캔(`@Scheduled`, 매일 직전 24시간) + 수동 실행(날짜범위 직접 지정, 비우면 최근 7일), 날짜범위 페이지네이션을 병렬 조회 후 키워드로 매칭, AI 적격판단(적합도 + 요약 + 추천 이유). 공고 클릭 시 게시글형 상세 페이지에서 AI 요약/추천 이유/원본 공고문 링크 확인(외부 알림 채널 없음)
- **Phase 3**: 문서 자동 채우기 — 참가신청서 등 `.docx` 문서를 업로드하면 원본 양식은 그대로 두고 회사자료실(회사소개서·인증서·사업자등록증 등)에서 확인 가능한 정보만 빈칸에 채워서 반환(모르는 항목은 비워둠), 다운로드
- **Phase 5**: 적격증빙자료 매칭 — 공고문 업로드 → 제출서류 추출 → Claude가 회사 증빙자료(회사자료실 "증빙서류" 카테고리)와 실제 서류 종류를 대조해 매칭 → 매칭된 파일 ZIP 생성, 부족한 항목은 사유와 함께 안내

> 아이디어 제안(원래의 Phase 4)은 사용자 요청으로 제거했습니다. `docs/DESIGN.md`에는 원래 설계 내용이 기록으로 남아있습니다.

## 나라장터 API 관련 주의사항
`getBidPblancListInfoServcPPSSrch` 오퍼레이션의 요청/응답 필드는 공개된 예제와 문서를 참고해 구현했으며, 공식 Swagger 전체를 확인하지는 못했습니다(`ai-engine/app/services/bid_scanner.py`). 실제 키로 처음 실행했을 때 응답 필드명이 다르면(예: 날짜 필드, URL 필드) `_parse_date` 및 `item.get(...)` 부분을 응답 형태에 맞게 조정해주세요. 파싱은 방어적으로 되어 있어 필드가 안 맞아도 크래시 없이 빈 값으로 처리됩니다.

이 API는 키워드/공고명 검색 파라미터가 없고 날짜범위 + 페이지네이션만 지원합니다. 그래서 "키워드별 검색"은 API 호출 단위가 아니라 **날짜범위 전체를 페이지네이션으로 가져온 뒤 제목을 키워드로 분류**하는 방식으로 구현되어 있습니다. 자동 스케줄러는 매일 정해진 시각에 직전 24시간을 고정 윈도우로 스캔하고(`BidScanWindow.java`), 사용자가 나라장터 페이지에서 "지금 스캔 실행"을 누를 때는 직접 지정한 날짜범위(비우면 최근 7일)로 조회합니다.

`anthropic` SDK 버전이 설치된 `httpx`와 맞지 않으면(`Client.__init__() got an unexpected keyword argument 'proxies'`) 적격판단 호출이 전부 실패합니다 — `ai-engine/requirements.txt`의 `anthropic`을 최신 버전으로 맞춰주세요(`pip install -U anthropic`). 개별 판단 호출이 실패해도(레이트리밋, 크레딧 부족 등) 스캔 전체가 죽지 않고 해당 공고만 "판단 보류"로 처리됩니다.

## 설계서 대비 구현 편차
Phase 2~5를 구현하며 설계서(`docs/DESIGN.md`) 원안에서 몇 가지를 실용적으로 조정했습니다 — 상세 이유는 문서 하단 참고:
- 아이디어 제안 기능: **제거** (사용자 요청)
- 알림 채널: Google Chat → ~~Slack~~ → **제거, 웹페이지에 직접 나열**로 재변경 (사용자 요청)
- 증빙자료 매칭: ~~로컬 sentence-transformers 임베딩 + 코사인 유사도~~ → **Claude가 항목·파일 전체를 직접 비교 판단**으로 재변경 (임베딩 유사도가 서로 다른 서류 종류를 구분 못 해 오매칭이 발생 — 사용자 신고로 발견)

## 보안 메모
- 실제 비밀정보(OAuth secret, API 키)는 `*.example` 템플릿만 커밋되고 실값은 로컬 `.env`/`application-local.yml`에만 존재합니다(`.gitignore` 처리).
- 회사 자료실 원본 파일은 현재 로컬 디스크(`backend/data/company-files`)에 저장됩니다. 배포 단계에서 `FileStorageService` 구현체를 S3로 교체할 예정입니다.
