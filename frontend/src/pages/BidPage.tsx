import { useEffect, useState } from 'react'
import {
  addKeyword,
  listKeywords,
  listRecentNotices,
  removeKeyword,
  scanNow,
  type BidKeyword,
  type BidNotice,
  type BidScanSummary,
} from '../api/bids'

const STATUS_LABELS: Record<BidNotice['status'], string> = {
  DETECTED: '감지됨',
  NOTIFIED: '알림 전송됨',
  IGNORED: '적격 기준 미달',
}

export default function BidPage() {
  const [keywords, setKeywords] = useState<BidKeyword[]>([])
  const [newKeyword, setNewKeyword] = useState('')
  const [notices, setNotices] = useState<BidNotice[]>([])
  const [isScanning, setIsScanning] = useState(false)
  const [summary, setSummary] = useState<BidScanSummary | null>(null)

  async function refresh() {
    const [keywordList, noticeList] = await Promise.all([listKeywords(), listRecentNotices()])
    setKeywords(keywordList)
    setNotices(noticeList)
  }

  useEffect(() => {
    refresh()
  }, [])

  async function handleAddKeyword() {
    if (!newKeyword.trim()) return
    await addKeyword(newKeyword.trim())
    setNewKeyword('')
    await refresh()
  }

  async function handleRemoveKeyword(id: number) {
    await removeKeyword(id)
    await refresh()
  }

  async function handleScanNow() {
    setIsScanning(true)
    try {
      setSummary(await scanNow())
      await refresh()
    } finally {
      setIsScanning(false)
    }
  }

  return (
    <div>
      <p className="text-xs font-medium tracking-[0.4em] text-muted">나라장터</p>
      <h1 className="mt-4 text-4xl font-semibold tracking-tight text-offwhite">나라장터 자동화</h1>
      <p className="mt-2 max-w-2xl text-sm text-muted">
        키워드에 매칭되는 신규 공고를 매일 자동으로 탐지하고, AI가 적격 여부를 판단해 Slack으로 알립니다.
      </p>

      <div className="mt-10 grid gap-6 md:grid-cols-[280px_1fr]">
        <section className="rounded-lg border border-hairline bg-navy-900 p-6">
          <h2 className="text-sm font-semibold text-offwhite">키워드</h2>
          <div className="mt-4 flex gap-2">
            <input
              value={newKeyword}
              onChange={(e) => setNewKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleAddKeyword()}
              placeholder="예: 공간정보"
              className="w-full rounded border border-hairline bg-navy-950 px-3 py-2 text-sm text-offwhite"
            />
            <button
              onClick={handleAddKeyword}
              className="shrink-0 rounded border border-hairline px-3 py-2 text-sm text-offwhite hover:border-accent"
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

          <button
            onClick={handleScanNow}
            disabled={isScanning}
            className="mt-8 w-full rounded border border-hairline py-2 text-sm text-offwhite hover:border-accent disabled:opacity-50"
          >
            {isScanning ? '스캔 중…' : '지금 스캔 실행'}
          </button>
          {summary && (
            <p className="mt-3 text-xs text-muted">
              조회 {summary.fetched}건 · 신규 {summary.newNotices}건 · 적격 {summary.eligibleCount}건 · 알림{' '}
              {summary.notifiedCount}건
            </p>
          )}
        </section>

        <section className="rounded-lg border border-hairline bg-navy-900 p-6">
          <h2 className="text-sm font-semibold text-offwhite">최근 공고</h2>
          <div className="mt-4 space-y-3">
            {notices.map((notice) => (
              <div key={notice.id} className="rounded border border-hairline/60 p-4">
                <div className="flex items-start justify-between gap-4">
                  <a
                    href={notice.url ?? undefined}
                    target="_blank"
                    rel="noreferrer"
                    className="text-sm text-offwhite hover:text-accent"
                  >
                    {notice.title}
                  </a>
                  <span className="shrink-0 text-xs text-muted">{STATUS_LABELS[notice.status]}</span>
                </div>
                <p className="mt-1 text-xs text-muted">
                  {notice.agency} · 키워드: {notice.matchedKeyword} · 마감:{' '}
                  {notice.deadline ?? '미상'}
                  {notice.eligibilityScore != null && ` · 적격점수 ${notice.eligibilityScore.toFixed(2)}`}
                </p>
                {notice.aiJudgement && <p className="mt-2 text-xs text-muted">{notice.aiJudgement}</p>}
              </div>
            ))}
            {notices.length === 0 && <p className="text-sm text-muted">아직 감지된 공고가 없습니다.</p>}
          </div>
        </section>
      </div>
    </div>
  )
}
