#!/usr/bin/env bash
# One-shot local dev stack: postgres + ai-engine + backend + frontend.
# Ctrl+C stops everything (including the postgres container).
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"
mkdir -p logs

PIDS=()
cleanup() {
	echo ""
	echo "▶ 전체 스택을 정지합니다..."
	for pid in "${PIDS[@]:-}"; do
		kill "$pid" >/dev/null 2>&1 || true
	done
	docker compose down >/dev/null 2>&1 || true
}
trap cleanup EXIT INT TERM

if [ ! -f backend/src/main/resources/application-local.yml ]; then
	echo "⚠️  backend/src/main/resources/application-local.yml 이 없습니다."
	echo "   cp backend/src/main/resources/application-local.yml.example backend/src/main/resources/application-local.yml 로 만들고 값을 채워주세요."
fi
if [ ! -f ai-engine/.env ]; then
	echo "⚠️  ai-engine/.env 가 없습니다."
	echo "   cp ai-engine/.env.example ai-engine/.env 로 만들고 값을 채워주세요."
fi

echo "▶ Postgres 기동..."
docker compose up -d postgres
until docker compose exec -T postgres pg_isready -U g2b_oa >/dev/null 2>&1; do
	sleep 1
done
echo "✔ Postgres 준비 완료"

echo "▶ AI Engine 준비..."
if [ ! -d ai-engine/.venv ]; then
	python3 -m venv ai-engine/.venv
	ai-engine/.venv/bin/pip install -q -r ai-engine/requirements.txt
fi
(cd ai-engine && source .venv/bin/activate && exec uvicorn app.main:app --port 8000) >logs/ai-engine.log 2>&1 &
PIDS+=($!)

echo "▶ Backend 기동..."
(cd backend && SPRING_PROFILES_ACTIVE=local exec ./gradlew bootRun) >logs/backend.log 2>&1 &
PIDS+=($!)

echo "▶ Frontend 준비..."
if [ ! -d frontend/node_modules ]; then
	(cd frontend && npm install)
fi
(cd frontend && exec npm run dev) >logs/frontend.log 2>&1 &
PIDS+=($!)

cat <<EOF

모든 서비스가 백그라운드에서 기동되었습니다:
  Frontend   http://localhost:5173
  Backend    http://localhost:8080
  AI Engine  http://localhost:8000  (Spring에서만 호출, 직접 열 필요 없음)

로그 확인:  tail -f logs/backend.log | logs/ai-engine.log | logs/frontend.log
정지하려면 Ctrl+C

EOF

wait
