import { useEffect, useState } from 'react'
import { isAxiosError } from 'axios'
import { Link } from 'react-router-dom'
import {
  MAX_KEYWORDS,
  addKeyword,
  fetchRecentEligible,
  listKeywords,
  removeKeyword,
  scanNow,
  type BidKeyword,
  type BidNotice,
  type BidScanSummary,
} from '../api/bids'
import type { ApiResponse } from '../api/client'

function errorMessage(err: unknown, fallback: string): string {
  return (isAxiosError<ApiResponse<unknown>>(err) ? err.response?.data?.message : null) || fallback
}

const STATUS_LABELS: Record<BidNotice['status'], string> = {
  ELIGIBLE: '적격',
  INELIGIBLE: '부적격',
}

export default function BidPage() {
  const [keywords, setKeywords] = useState<BidKeyword[]>([])
  const [newKeyword, setNewKeyword] = useState('')
  const [notices, setNotices] = useState<BidNotice[]>([])
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [isScanning, setIsScanning] = useState(false)
  const [isAddingKeyword, setIsAddingKeyword] = useState(false)
  // 페이지를 새로 열 때마다 "지금 스캔 실행"을 눌러야만 적격 공고를 보여준다 — 예전 스캔
  // 결과(자동 스캔 포함)가 사용자 액션 없이 뜨는 게 "왜 벌써 결과가 있지?"라는 혼란을 줬음.
  // 데이터는 그대로 두고 화면 표시만 이 세션의 스캔 실행 여부로 게이트한다.
  const [hasScanned, setHasScanned] = useState(false)
  const [summary, setSummary] = useState<BidScanSummary | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function refresh(rangeStart = startDate, rangeEnd = endDate) {
    const [keywordList, noticeList] = await Promise.all([
      listKeywords(),
      fetchRecentEligible(rangeStart || undefined, rangeEnd || undefined),
    ])
    setKeywords(keywordList)
    setNotices(noticeList)
  }

  function validateRange(): string | null {
    if (Boolean(startDate) !== Boolean(endDate)) {
      return '조회 시작일과 종료일을 모두 입력하거나, 모두 비워주세요.'
    }
    if (startDate && endDate && startDate > endDate) {
      return '조회 시작일은 종료일보다 늦을 수 없습니다.'
    }
    return null
  }

  useEffect(() => {
    refresh()
  }, [])

  async function handleAddKeyword() {
    if (!newKeyword.trim() || keywords.length >= MAX_KEYWORDS || isAddingKeyword) return
    setIsAddingKeyword(true)
    setError(null)
    try {
      await addKeyword(newKeyword.trim())
      setNewKeyword('')
      await refresh()
    } catch (err) {
      setError(errorMessage(err, '키워드 추가 중 오류가 발생했습니다.'))
    } finally {
      setIsAddingKeyword(false)
    }
  }

  async function handleRemoveKeyword(id: number) {
    setError(null)
    try {
      await removeKeyword(id)
      await refresh()
    } catch (err) {
      setError(errorMessage(err, '키워드 삭제 중 오류가 발생했습니다.'))
    }
  }

  async function handleScanNow() {
    const validationError = validateRange()
    if (validationError) {
      setError(validationError)
      return
    }
    setIsScanning(true)
    setError(null)
    try {
      setSummary(await scanNow(startDate || undefined, endDate || undefined))
      await refresh()
      setHasScanned(true)
    } catch (err) {
      setError(errorMessage(err, '스캔 중 오류가 발생했습니다.'))
    } finally {
      setIsScanning(false)
    }
  }

  return (
    <div>
      <p className="text-xs font-medium tracking-[0.4em] text-muted">나라장터</p>
      <h1 className="mt-4 text-4xl font-semibold tracking-tight text-offwhite">나라장터 자동화</h1>
      <p className="mt-2 max-w-2xl text-sm text-muted">
        키워드에 매칭되는 공고를 지정한 기간 동안 탐지하고, AI가 회사 자료와 비교해 적격 여부를 판단합니다.
      </p>

      <div className="mt-10 grid gap-6 md:grid-cols-[280px_1fr]">
        <section className="rounded-lg border border-hairline bg-navy-900 p-6">
          <div className="flex items-baseline justify-between">
            <h2 className="text-sm font-semibold text-offwhite">키워드</h2>
            <span className="text-xs text-muted">
              {keywords.length}/{MAX_KEYWORDS}
            </span>
          </div>
          <div className="mt-4 flex gap-2">
            <input
              value={newKeyword}
              onChange={(e) => setNewKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleAddKeyword()}
              placeholder="예: 공간정보"
              disabled={keywords.length >= MAX_KEYWORDS || isAddingKeyword}
              className="w-full rounded border border-hairline bg-navy-950 px-3 py-2 text-sm text-offwhite disabled:opacity-50"
            />
            <button
              onClick={handleAddKeyword}
              disabled={keywords.length >= MAX_KEYWORDS || isAddingKeyword}
              className="shrink-0 rounded border border-hairline px-3 py-2 text-sm text-offwhite hover:border-accent disabled:opacity-50"
            >
              추가
            </button>
          </div>
          <ul className="mt-4 flex flex-wrap gap-2">
            {keywords.map((kw) => (
              <li
                key={kw.id}
                className="flex items-center gap-2 rounded-full border border-hairline px-3 py-1 text-xs text-muted"
              >
                {kw.keyword}
                <button onClick={() => handleRemoveKeyword(kw.id)} className="hover:text-offwhite">
                  ×
                </button>
              </li>
            ))}
            {keywords.length === 0 && <li className="text-xs text-muted">등록된 키워드가 없습니다.</li>}
          </ul>
          {error && <p className="mt-3 text-xs text-pending">{error}</p>}

          <h2 className="mt-8 text-sm font-semibold text-offwhite">조회 기간</h2>
          <p className="mt-1 text-xs text-muted">비워두면 최근 7일을 조회합니다.</p>
          <div className="mt-3 space-y-2">
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="w-full rounded border border-hairline bg-navy-950 px-3 py-2 text-sm text-offwhite"
            />
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="w-full rounded border border-hairline bg-navy-950 px-3 py-2 text-sm text-offwhite"
            />
          </div>

          <button
            onClick={handleScanNow}
            disabled={isScanning}
            className="btn-premium mt-6 w-full rounded-full border border-hairline py-2.5 text-sm tracking-wide text-offwhite hover:border-accent hover:text-accent disabled:opacity-50"
          >
            {isScanning ? '스캔 중…' : '지금 스캔 실행'}
          </button>
          {summary && (
            <p className="mt-3 text-xs leading-relaxed text-muted">
              조회 기간 {summary.rangeStart} ~ {summary.rangeEnd}
              <br />
              조회 {summary.fetched}건 · 매칭 {summary.matched}건 · 신규 {summary.newNotices}건 · 적격{' '}
              {summary.eligibleCount}건
              {summary.matched > 0 && (
                <>
                  <br />
                  AI 판단 완료 {summary.judged}건 · 판단 보류 {summary.unjudged}건
                  {summary.unjudged > 0 && ' (AI 호출 실패 또는 회사 자료 부족 — 크레딧/API 키 확인 필요)'}
                </>
              )}
            </p>
          )}
        </section>

        <section className="rounded-lg border border-hairline bg-navy-900 p-6">
          <h2 className="text-sm font-semibold text-offwhite">적격 공고</h2>
          <div className="mt-4 space-y-3">
            {!hasScanned && (
              <p className="text-sm text-muted">
                키워드를 등록하고 "지금 스캔 실행"을 눌러 공고를 조회해보세요.
              </p>
            )}
            {hasScanned &&
              notices.map((notice) => (
                <Link
                  key={notice.id}
                  to={`/bids/${notice.id}`}
                  className="btn-premium block rounded border border-hairline/60 p-4 hover:border-accent"
                >
                  <div className="flex items-start justify-between gap-4">
                    <span className="text-sm text-offwhite">{notice.title}</span>
                    <span className="shrink-0 text-xs text-muted">{STATUS_LABELS[notice.status]}</span>
                  </div>
                  <p className="mt-1 text-xs text-muted">
                    {notice.agency} · 키워드: {notice.matchedKeyword} · 마감: {notice.deadline ?? '미상'}
                    {notice.eligibilityScore != null && ` · 적격점수 ${notice.eligibilityScore.toFixed(2)}`}
                  </p>
                  {notice.aiSummary && <p className="mt-2 line-clamp-2 text-xs text-muted">{notice.aiSummary}</p>}
                </Link>
              ))}
            {hasScanned && notices.length === 0 && (
              <p className="text-sm text-muted">아직 감지된 적격 공고가 없습니다.</p>
            )}
          </div>
        </section>
      </div>
    </div>
  )
}
