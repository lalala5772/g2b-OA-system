import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchBidDetail, type BidNotice } from '../api/bids'

export default function BidDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [notice, setNotice] = useState<BidNotice | null>(null)

  useEffect(() => {
    if (id) {
      fetchBidDetail(Number(id)).then(setNotice)
    }
  }, [id])

  if (!notice) {
    return <p className="text-sm text-muted">불러오는 중…</p>
  }

  return (
    <div>
      <Link to="/bids" className="text-xs text-muted transition-colors hover:text-offwhite">
        ← 목록으로
      </Link>

      <p className="mt-6 text-xs font-medium tracking-[0.4em] text-muted">
        {notice.status === 'ELIGIBLE' ? '적격 공고' : '부적격 공고'}
      </p>
      <h1 className="mt-4 text-3xl font-semibold leading-snug tracking-tight text-offwhite md:text-4xl">
        {notice.title}
      </h1>

      <div className="mt-4 flex flex-wrap gap-x-4 gap-y-1 text-sm text-muted">
        {notice.agency && <span>발주기관 {notice.agency}</span>}
        {notice.matchedKeyword && <span>키워드 {notice.matchedKeyword}</span>}
        {notice.announceDate && <span>공고일 {notice.announceDate}</span>}
        {notice.deadline && <span>마감일 {notice.deadline}</span>}
      </div>

      {notice.eligibilityScore != null && (
        <p className="mt-6 inline-block rounded-full border border-hairline px-4 py-1.5 text-sm text-accent">
          적합도 {(notice.eligibilityScore * 100).toFixed(0)}%
        </p>
      )}

      {notice.aiSummary && (
        <section className="mt-8">
          <h2 className="text-xs font-semibold uppercase tracking-wide text-muted">공고 요약</h2>
          <p className="mt-3 max-w-2xl text-sm leading-relaxed text-offwhite">{notice.aiSummary}</p>
        </section>
      )}

      {notice.aiJudgement && (
        <section className="mt-8">
          <h2 className="text-xs font-semibold uppercase tracking-wide text-muted">추천 이유</h2>
          <p className="mt-3 max-w-2xl text-sm leading-relaxed text-offwhite">{notice.aiJudgement}</p>
        </section>
      )}

      {notice.url && (
        <a
          href={notice.url}
          target="_blank"
          rel="noreferrer"
          className="btn-premium mt-10 inline-block rounded-full border border-hairline px-6 py-3 text-sm tracking-wide text-offwhite hover:border-accent hover:text-accent"
        >
          원본 공고문 보기
        </a>
      )}
    </div>
  )
}
