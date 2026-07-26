import { useEffect, useState } from 'react'
import {
  downloadUrl,
  generateDocument,
  listTemplates,
  uploadTemplate,
  type DocumentGenerationResult,
  type DocumentTemplate,
} from '../api/documents'

const EXAMPLE_SCHEMA = '[{"key":"companyName","label":"회사명","auto":true},{"key":"projectName","label":"사업명","auto":false}]'

export default function DocumentPage() {
  const [templates, setTemplates] = useState<DocumentTemplate[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [fieldValues, setFieldValues] = useState<Record<string, string>>({})
  const [result, setResult] = useState<DocumentGenerationResult | null>(null)
  const [isGenerating, setIsGenerating] = useState(false)

  const [templateFile, setTemplateFile] = useState<File | null>(null)
  const [templateName, setTemplateName] = useState('')
  const [schemaText, setSchemaText] = useState(EXAMPLE_SCHEMA)

  async function refresh() {
    const list = await listTemplates()
    setTemplates(list)
    if (list.length > 0 && selectedId === null) {
      setSelectedId(list[0].id)
    }
  }

  useEffect(() => {
    refresh()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const selected = templates.find((t) => t.id === selectedId) ?? null
  const manualFields = selected?.fields.filter((f) => !f.auto) ?? []

  async function handleUploadTemplate() {
    if (!templateFile || !templateName.trim()) return
    const fields = JSON.parse(schemaText)
    await uploadTemplate(templateFile, templateName.trim(), fields)
    setTemplateFile(null)
    setTemplateName('')
    await refresh()
  }

  async function handleGenerate() {
    if (!selectedId) return
    setIsGenerating(true)
    try {
      setResult(await generateDocument(selectedId, fieldValues))
    } finally {
      setIsGenerating(false)
    }
  }

  return (
    <div>
      <p className="text-xs font-medium tracking-[0.4em] text-muted">문서</p>
      <h1 className="mt-4 text-4xl font-semibold tracking-tight text-offwhite">문서 자동 채우기</h1>
      <p className="mt-2 max-w-2xl text-sm text-muted">
        Word 양식을 선택하면 회사 고정정보는 자료실에서 자동으로 채우고, 이번 건에만 해당하는 항목만 입력받습니다.
      </p>

      <div className="mt-10 grid gap-6 md:grid-cols-2">
        <section className="space-y-6">
          <div className="rounded-lg border border-hairline bg-navy-900 p-6">
            <h2 className="text-sm font-semibold text-offwhite">템플릿 선택</h2>
            <select
              value={selectedId ?? ''}
              onChange={(e) => setSelectedId(Number(e.target.value))}
              className="mt-4 w-full rounded border border-hairline bg-navy-950 px-3 py-2 text-sm text-offwhite"
            >
              {templates.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.name}
                </option>
              ))}
              {templates.length === 0 && <option value="">등록된 템플릿이 없습니다</option>}
            </select>

            {manualFields.length > 0 && (
              <div className="mt-4 space-y-3">
                {manualFields.map((field) => (
                  <div key={field.key}>
                    <label className="text-xs text-muted">{field.label}</label>
                    <input
                      value={fieldValues[field.key] ?? ''}
                      onChange={(e) => setFieldValues((prev) => ({ ...prev, [field.key]: e.target.value }))}
                      className="mt-1 w-full rounded border border-hairline bg-navy-950 px-3 py-2 text-sm text-offwhite"
                    />
                  </div>
                ))}
              </div>
            )}

            <button
              onClick={handleGenerate}
              disabled={!selectedId || isGenerating}
              className="mt-6 w-full rounded border border-hairline py-2 text-sm text-offwhite hover:border-accent disabled:opacity-50"
            >
              {isGenerating ? '생성 중…' : '문서 생성'}
            </button>
          </div>

          <details className="rounded-lg border border-hairline bg-navy-900 p-6 text-sm text-muted">
            <summary className="cursor-pointer text-offwhite">새 템플릿 업로드</summary>
            <div className="mt-4 space-y-3">
              <input
                type="file"
                accept=".docx"
                onChange={(e) => setTemplateFile(e.target.files?.[0] ?? null)}
                className="block text-xs"
              />
              <input
                value={templateName}
                onChange={(e) => setTemplateName(e.target.value)}
                placeholder="템플릿 이름"
                className="w-full rounded border border-hairline bg-navy-950 px-3 py-2 text-sm text-offwhite"
              />
              <textarea
                value={schemaText}
                onChange={(e) => setSchemaText(e.target.value)}
                rows={3}
                className="w-full rounded border border-hairline bg-navy-950 px-3 py-2 font-mono text-xs text-offwhite"
              />
              <button
                onClick={handleUploadTemplate}
                className="rounded border border-hairline px-4 py-2 text-xs text-offwhite hover:border-accent"
              >
                업로드
              </button>
            </div>
          </details>
        </section>

        <section className="rounded-lg border border-hairline bg-navy-900 p-6">
          <h2 className="text-sm font-semibold text-offwhite">결과</h2>
          {!result && <p className="mt-4 text-sm text-muted">아직 생성된 문서가 없습니다.</p>}
          {result && (
            <div className="mt-4 space-y-4">
              <p className="text-sm text-muted">
                상태: {result.status === 'SUCCESS' ? '생성 완료' : '생성 실패'}
              </p>
              {Object.keys(result.autoFilledFields).length > 0 && (
                <div>
                  <p className="text-xs text-muted">자동으로 채워진 항목</p>
                  <ul className="mt-2 space-y-1 text-sm text-offwhite">
                    {Object.entries(result.autoFilledFields).map(([key, value]) => (
                      <li key={key}>
                        {key}: {value || '(추출 실패)'}
                      </li>
                    ))}
                  </ul>
                </div>
              )}
              {result.status === 'SUCCESS' && (
                <a
                  href={downloadUrl(result.id)}
                  className="inline-block rounded border border-hairline px-4 py-2 text-sm text-offwhite hover:border-accent"
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
