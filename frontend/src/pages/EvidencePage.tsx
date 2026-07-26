import { useState } from 'react'
import FileUploader from '../components/FileUploader'
import { uploadFile } from '../api/files'
import { analyzeRequirements, zipDownloadUrl, type EvidenceAnalysisResult } from '../api/evidence'

export default function EvidencePage() {
  const [result, setResult] = useState<EvidenceAnalysisResult | null>(null)
  const [isAnalyzing, setIsAnalyzing] = useState(false)

  async function handleFileSelected(files: File[]) {
    const file = files[0]
    if (!file) return
    setIsAnalyzing(true)
    try {
      const uploaded = await uploadFile(file, 'REQUIREMENT_DOC')
      setResult(await analyzeRequirements(uploaded.id))
    } finally {
      setIsAnalyzing(false)
    }
  }

  const matchedItems = result?.items.filter((item) => item.matched) ?? []
  const missingItems = result?.items.filter((item) => !item.matched) ?? []

  return (
    <div>
      <p className="text-xs font-medium tracking-[0.4em] text-muted">증빙</p>
      <h1 className="mt-4 text-4xl font-semibold tracking-tight text-offwhite">적격증빙자료 매칭</h1>
      <p className="mt-2 max-w-2xl text-sm text-muted">
        공모전/정부사업 공고문을 업로드하면 제출서류 목록을 추출하고 자료실 증빙자료와 자동 매칭해 ZIP으로 묶어드립니다.
      </p>

      <div className="mt-10 grid gap-6 md:grid-cols-2">
        <section className="rounded-lg border border-hairline bg-navy-900 p-6">
          <h2 className="text-sm font-semibold text-offwhite">공고문 업로드</h2>
          <p className="mt-2 text-xs text-muted">증빙자료는 자료실(카테고리: 증빙서류)에 미리 등록되어 있어야 합니다.</p>
          <div className="mt-4">
            <FileUploader onFilesSelected={handleFileSelected} accept=".docx,.pdf,.hwp,.hwpx" multiple={false} />
          </div>
          {isAnalyzing && <p className="mt-4 animate-pulse text-xs text-muted">— 분석 중 —</p>}
        </section>

        <section className="rounded-lg border border-hairline bg-navy-900 p-6">
          <h2 className="text-sm font-semibold text-offwhite">매칭 결과</h2>
          {!result && <p className="mt-4 text-sm text-muted">아직 분석된 요건이 없습니다.</p>}
          {result && (
            <div className="mt-4 space-y-6">
              <div>
                <div className="flex items-center justify-between">
                  <p className="text-xs text-muted">자동으로 준비된 서류 ({matchedItems.length}건)</p>
                  {result.zipExportId && (
                    <a
                      href={zipDownloadUrl(result.zipExportId)}
                      className="btn-premium rounded-full border border-hairline px-4 py-1.5 text-xs tracking-wide text-offwhite hover:border-accent hover:text-accent"
                    >
                      ZIP 다운로드
                    </a>
                  )}
                </div>
                <ul className="mt-2 space-y-1 text-sm text-offwhite">
                  {matchedItems.map((item) => (
                    <li key={item.id}>
                      {item.itemName} — {item.matchedFileName}
                      {item.confidenceScore != null && (
                        <span className="text-xs text-muted"> ({item.confidenceScore.toFixed(2)})</span>
                      )}
                    </li>
                  ))}
                  {matchedItems.length === 0 && <li className="text-xs text-muted">없음</li>}
                </ul>
              </div>

              <div>
                <p className="text-xs text-muted">부족한 자료 ({missingItems.length}건)</p>
                <ul className="mt-2 space-y-1 text-sm text-muted">
                  {missingItems.map((item) => (
                    <li key={item.id}>
                      {item.itemName} — {item.description || '해당하는 문서를 찾지 못했습니다.'}
                    </li>
                  ))}
                  {missingItems.length === 0 && <li className="text-xs text-muted">없음</li>}
                </ul>
              </div>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
