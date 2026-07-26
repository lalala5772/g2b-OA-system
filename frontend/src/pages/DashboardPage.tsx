import { useEffect, useState } from 'react'
import FeatureCard from '../components/FeatureCard'
import { fetchDashboardSummary, type DashboardSummary } from '../api/dashboard'

export default function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null)

  useEffect(() => {
    fetchDashboardSummary().then(setSummary)
  }, [])

  return (
    <div>
      <p className="text-xs font-medium tracking-[0.4em] text-muted">DASHBOARD</p>
      <h1 className="mt-4 text-4xl font-semibold tracking-tight text-offwhite">
        메인화면
      </h1>

      <div className="mt-10 grid gap-5 md:grid-cols-3">
        <FeatureCard
          to="/documents"
          title="문서 자동 채우기"
          description="Word 양식에 회사 고정정보와 입력값을 자동으로 채워 넣습니다."
          badge={summary?.document.badge}
        />
        <FeatureCard
          to="/evidence"
          title="적격증빙자료 매칭"
          description="사업 공고문의 제출서류를 분석해 자료실 증빙자료와 자동 매칭합니다."
          badge={summary?.evidence.badge}
        />
        <FeatureCard
          to="/bids"
          title="나라장터 자동화"
          description="키워드 기반으로 신규 공고를 탐지하고 적격 여부를 판단해 알립니다."
          badge={summary?.bid.badge}
        />
      </div>

      {summary && (
        <p className="mt-8 text-sm text-muted">
          회사 자료실에 {summary.totalCompanyFiles}건의 자료가 등록되어 있습니다.
        </p>
      )}
    </div>
  )
}
