import { useEffect, useState } from 'react'
import FileUploader from '../components/FileUploader'
import { uploadFile } from '../api/files'
import { generateIdeas, listRecentIdeas, type ContestIdea } from '../api/ideas'

export default function IdeaPage() {
  const [ideas, setIdeas] = useState<ContestIdea[]>([])
  const [isGenerating, setIsGenerating] = useState(false)
  const [statusMessage, setStatusMessage] = useState<string | null>(null)

  async function refresh() {
    setIdeas(await listRecentIdeas())
  }

  useEffect(() => {
    refresh()
  }, [])

  async function handleFileSelected(files: File[]) {
    const file = files[0]
    if (!file) return
    setIsGenerating(true)
    setStatusMessage(null)
    try {
      const uploaded = await uploadFile(file, 'CONTEST')
      const result = await generateIdeas(uploaded.id)
      if (result.status !== 'COMPLETED') {
        setStatusMessage('아이디어를 생성하지 못했습니다. 자료실에 도메인소개 자료가 등록되어 있는지 확인해주세요.')
      }
      await refresh()
    } finally {
      setIsGenerating(false)
    }
  }

  return (
    <div>
      <p className="text-xs font-medium tracking-[0.4em] text-muted">아이디어</p>
      <h1 className="mt-4 text-4xl font-semibold tracking-tight text-offwhite">아이디어 제안</h1>
      <p className="mt-2 max-w-2xl text-sm text-muted">
        공모전 파일을 업로드하면 자료실의 회사 소개 자료를 자동으로 참고해 아이디어를 제안합니다.
      </p>

      <div className="mt-10 grid gap-6 md:grid-cols-2">
        <section className="rounded-lg border border-hairline bg-navy-900 p-6">
          <h2 className="text-sm font-semibold text-offwhite">공모전 파일 업로드</h2>
          <p className="mt-2 text-xs text-muted">
            회사 자료는 별도로 올릴 필요 없이 자료실에서 자동 조회됩니다.
          </p>
          <div className="mt-4">
            <FileUploader onFilesSelected={handleFileSelected} accept=".docx,.pdf,.hwp,.hwpx" multiple={false} />
          </div>
          {isGenerating && <p className="mt-4 animate-pulse text-xs text-muted">— 분석 중 —</p>}
          {statusMessage && <p className="mt-4 text-xs text-muted">{statusMessage}</p>}
        </section>

        <section className="rounded-lg border border-hairline bg-navy-900 p-6">
          <h2 className="text-sm font-semibold text-offwhite">제안된 아이디어</h2>
          <div className="mt-4 space-y-4">
            {ideas.map((idea) => (
              <div key={idea.id} className="rounded border border-hairline/60 p-4">
                <div className="flex items-start justify-between gap-4">
                  <h3 className="text-sm text-offwhite">{idea.ideaTitle}</h3>
                  {idea.relevanceScore != null && (
                    <span className="shrink-0 text-xs text-muted">적합도 {idea.relevanceScore.toFixed(2)}</span>
                  )}
                </div>
                <p className="mt-2 text-xs leading-relaxed text-muted">{idea.ideaContent}</p>
              </div>
            ))}
            {ideas.length === 0 && <p className="text-sm text-muted">아직 제안된 아이디어가 없습니다.</p>}
          </div>
        </section>
      </div>
    </div>
  )
}
