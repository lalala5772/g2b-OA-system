# b2g-OA-system

업무 자동화 플랫폼 — Spring Boot(핵심 서비스) + FastAPI(AI/문서 엔진) + React 하이브리드 구조.

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

**`backend/src/main/resources/application-local.yml`**
- `spring.security.oauth2.client.registration.google.client-id` / `client-secret` — [Google Cloud Console](https://console.cloud.google.com/apis/credentials)에서 발급
- `app.jwt.secret` — 32바이트 이상 임의 문자열
- `ai-engine.api-key` — 아래 `AI_ENGINE_API_KEY`와 반드시 동일한 값(Spring↔FastAPI 내부 통신용)

**`ai-engine/.env`**
- `AI_ENGINE_API_KEY` — 위와 동일한 값
- `CLAUDE_API_KEY` / `CLAUDE_MODEL` — 나라장터 적격판단·문서 필드 자동추출·증빙 요건 추출에 사용
- `NARAJANGTEO_SERVICE_KEY` — 나라장터 Open API **디코딩키**(인코딩키 아님 — 인코딩키를 넣으면 이중 인코딩되어 인증 오류가 납니다)

Claude/나라장터 키가 없어도 앱은 정상적으로 뜨고, 해당 기능만 "결과 없음"으로 정직하게 응답합니다(크래시하지 않음).

### 한 번에 실행
사전 준비(위 0단계)만 끝냈다면, 아래 명령 하나로 Postgres·AI Engine·Backend·Frontend를 전부 백그라운드로 띄웁니다:
```bash
./scripts/dev.sh
```
최초 실행 시 `ai-engine/.venv`와 `frontend/node_modules`가 없으면 자동으로 생성. `Ctrl+C`를 누르면 4개 프로세스와 Postgres 컨테이너까지 한 번에 정리. 개별 로그는 `logs/backend.log`, `logs/ai-engine.log`, `logs/frontend.log`에서 확인(`tail -f logs/backend.log`)

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
나라장터 자동 스캔은 매일 `app.bid.scan-cron`(기본 10:00, `BID_SCAN_CRON`으로 변경 가능) 시각에 자동 실행. 즉시 테스트하려면 나라장터 페이지의 "지금 스캔 실행" 버튼을 사용. 키워드는 `BID_KEYWORDS_SEED`(콤마 구분, 최초 기동 시 1회 시드) 환경변수나 나라장터 페이지에서 직접 추가.

### 4. Frontend
```bash
cd frontend
npm install
npm run dev
```

## 현재 구현 범위

* **Phase 1**: Google OAuth2 로그인, 대시보드 허브, 회사 자료실 구현

  * 회사 자료 업로드/파싱/목록 조회/삭제 지원
  * 업로드 시 왼쪽에서 선택한 카테고리로만 저장됨
  * 목록 필터와 업로드 대상이 동일한 상태를 공유하도록 구현함

* **Phase 2**: 나라장터 자동화 구현

  * 키워드 최대 15개 등록 가능
  * 자동 스캔: `@Scheduled`를 활용해 매일 직전 24시간 범위 조회
  * 수동 실행: 사용자가 날짜 범위를 직접 지정할 수 있으며, 범위를 입력하지 않으면 최근 7일 조회
  * 날짜 범위 내 공고를 페이지네이션으로 병렬 조회한 후 등록된 키워드 기준으로 매칭
  * AI를 활용해 적격 여부, 적합도, 공고 요약, 추천 이유 판단
  * 공고 클릭 시 게시글 형태의 상세 페이지 제공
  * 상세 페이지에서 AI 요약, 추천 이유, 원본 공고문 링크 확인 가능
  * 외부 알림 채널은 사용하지 않고 웹페이지에서 결과를 직접 확인하도록 구현함

* **Phase 3**: 문서 자동 채우기 구현

  * 참가신청서 등의 `.docx` 문서 업로드 가능
  * 원본 문서 양식은 유지하고 빈칸만 자동으로 채움
  * 회사 자료실의 회사소개서, 인증서, 사업자등록증 등에서 확인 가능한 정보만 입력
  * 확인할 수 없는 항목은 임의로 입력하지 않고 빈칸으로 유지
  * 작성된 문서 다운로드 지원

* **Phase 5**: 적격증빙자료 매칭 구현

  * 공고문 업로드 시 제출서류 목록 자동 추출
  * Claude를 활용해 제출서류 항목과 회사 자료실의 실제 증빙자료 종류를 직접 비교
  * 회사 자료실의 "증빙서류" 카테고리 내 파일을 대상으로 매칭
  * 매칭된 증빙자료를 ZIP 파일로 생성
  * 증빙자료가 부족한 항목은 부족 사유와 함께 안내

> 기존 **Phase 4(아이디어 제안 기능)**는 사용자 요청으로 제거함.
> `docs/DESIGN.md`에는 기존 설계 내용이 기록으로 남아 있음.

## 나라장터 API 관련 주의사항

* `getBidPblancListInfoServcPPSSrch` 오퍼레이션의 요청/응답 필드는 공개된 예제와 문서를 참고해 구현함
* 공식 Swagger 전체를 확인한 것은 아니므로 실제 API 키로 최초 실행 시 응답 필드명이 다를 가능성 있음
* 응답 필드명이 실제 API와 다른 경우 `ai-engine/app/services/bid_scanner.py`의 `_parse_date` 및 `item.get(...)` 부분을 실제 응답 형태에 맞게 수정해야 함
* 현재 파싱 로직은 방어적으로 구현되어 있어 필드명이 맞지 않더라도 크래시 없이 빈 값으로 처리하도록 구성함

### 키워드 검색 방식

* 해당 API는 키워드/공고명 검색 파라미터를 제공하지 않음
* 날짜 범위와 페이지네이션을 기준으로 공고 전체를 조회하는 방식임
* 따라서 "키워드별 검색"을 별도의 API 호출로 처리하지 않고, **날짜 범위 전체를 페이지네이션으로 조회한 후 공고 제목을 등록된 키워드와 비교해 분류**하도록 구현함

### 스캔 방식

* 자동 스케줄러

  * 매일 정해진 시각에 실행
  * 직전 24시간을 고정된 스캔 범위로 사용
  * `BidScanWindow.java`에서 스캔 범위 관리

* 수동 스캔

  * 나라장터 페이지에서 "지금 스캔 실행" 버튼으로 실행
  * 사용자가 직접 날짜 범위 지정 가능
  * 날짜 범위를 입력하지 않으면 최근 7일을 대상으로 조회

## AI API 관련 주의사항

* `anthropic` SDK와 설치된 `httpx` 버전이 맞지 않을 경우 다음과 같은 오류가 발생할 수 있음

  * `Client.__init__() got an unexpected keyword argument 'proxies'`
* 이 경우 `ai-engine/requirements.txt`의 `anthropic` 버전을 최신 버전으로 설정하고 `pip install -U anthropic` 실행 필요
* 개별 AI 판단 요청이 실패하더라도 스캔 전체가 중단되지 않도록 구현함
* 레이트리밋, 크레딧 부족 등의 문제로 개별 판단이 실패하면 해당 공고만 **"판단 보류"** 상태로 처리함

## 설계서 대비 구현 편차

Phase 2~5 구현 과정에서 기존 설계서(`docs/DESIGN.md`)의 원안을 일부 실용적으로 변경함.

* **아이디어 제안 기능**

  * 기존: 구현 예정
  * 변경: **제거**
  * 사유: 사용자 요청

* **알림 채널**

  * 기존: Google Chat
  * 변경: ~~Slack~~ → **외부 알림 기능 제거**
  * 최종 방식: 웹페이지에서 공고 결과를 직접 확인
  * 사유: 사용자 요청

* **증빙자료 매칭**

  * 기존: 로컬 `sentence-transformers` 임베딩 + 코사인 유사도
  * 변경: **Claude가 제출 항목과 증빙 파일 전체를 직접 비교하여 판단**
  * 사유: 임베딩 유사도 방식으로는 서로 다른 서류 종류가 유사하게 판단되어 오매칭이 발생하는 문제가 있었음
  * 실제 사용자 테스트 과정에서 해당 문제를 확인하여 Claude 직접 비교 방식으로 변경함

## 보안 메모

* OAuth Secret, API Key 등은 `*.example` 템플릿만 커밋하고 실제 값은 로컬 `.env` 또는 `application-local.yml`에서 관리
* 관련 파일은 `.gitignore`로 Git 추적 대상에서 제외
* 회사 자료실의 원본 파일은 현재 로컬 디스크 `backend/data/company-files`에 저장
* 배포 단계에서는 `FileStorageService` 구현체를 S3 기반으로 교체할 예정

