import { useState } from 'react'
import { autoFillDocument, downloadUrl, type DocumentAutoFillResult } from '../api/documents'

export default function DocumentPage() {
  const [file, setFile] = useState<File | null>(null)
  const [result, setResult] = useState<DocumentAutoFillResult | null>(null)
  const [isFilling, setIsFilling] = useState(false)

  async function handleAutoFill() {
    if (!file) return
    setIsFilling(true)
    try {
      setResult(await autoFillDocument(file))
    } finally {
      setIsFilling(false)
    }
  }

  return (
    <div>
      <p className="text-xs font-medium tracking-[0.4em] text-muted">문서</p>
      <h1 className="mt-4 text-4xl font-semibold tracking-tight text-offwhite">문서 자동 채우기</h1>
      <p className="mt-2 max-w-2xl text-sm text-muted">
        참가신청서 등 회사정보 입력이 필요한 문서를 업로드하면, 원래 양식은 그대로 두고 회사자료실에서
        확인 가능한 정보만 빈칸에 채워서 돌려드립니다. 자료실에서 알 수 없는 항목은 비워둡니다.
      </p>

      <div className="mt-10 grid gap-6 md:grid-cols-2">
        <section className="rounded-lg border border-hairline bg-navy-900 p-6">
          <h2 className="text-sm font-semibold text-offwhite">문서 업로드</h2>
          <p className="mt-1 text-xs text-muted">.docx 파일만 지원합니다.</p>
          <input
            type="file"
            accept=".docx"
            onChange={(e) => {
              setFile(e.target.files?.[0] ?? null)
              setResult(null)
            }}
            className="mt-4 block text-xs text-muted"
          />
          <button
            onClick={handleAutoFill}
            disabled={!file || isFilling}
            className="btn-premium mt-6 w-full rounded-full border border-hairline py-2.5 text-sm tracking-wide text-offwhite hover:border-accent hover:text-accent disabled:opacity-50"
          >
            {isFilling ? '채우는 중…' : '자동 채우기 실행'}
          </button>
        </section>

        <section className="rounded-lg border border-hairline bg-navy-900 p-6">
          <h2 className="text-sm font-semibold text-offwhite">결과</h2>
          {!result && <p className="mt-4 text-sm text-muted">아직 채운 문서가 없습니다.</p>}
          {result && (
            <div className="mt-4 space-y-4">
              <p className="text-sm text-muted">상태: {result.status === 'SUCCESS' ? '생성 완료' : '생성 실패'}</p>
              {Object.keys(result.filledFields).length > 0 ? (
                <div>
                  <p className="text-xs text-muted">자동으로 채워진 항목</p>
                  <ul className="mt-2 space-y-1 text-sm text-offwhite">
                    {Object.entries(result.filledFields).map(([label, value]) => (
                      <li key={label}>
                        {label}: {value}
                      </li>
                    ))}
                  </ul>
                </div>
              ) : (
                <p className="text-sm text-muted">회사자료실에서 채울 수 있는 항목을 찾지 못했습니다.</p>
              )}
              {result.status === 'SUCCESS' && (
                <a
                  href={downloadUrl(result.id)}
                  className="btn-premium inline-block rounded-full border border-hairline px-5 py-2 text-sm tracking-wide text-offwhite hover:border-accent hover:text-accent"
                >
                  다운로드
                </a>
              )}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
