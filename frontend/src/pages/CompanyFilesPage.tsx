import { useEffect, useState } from 'react'
import FileUploader from '../components/FileUploader'
import {
  CATEGORY_LABELS,
  deleteCompanyFile,
  listCompanyFiles,
  uploadCompanyFile,
  type CompanyFile,
  type FileCategory,
} from '../api/companyFiles'

const CATEGORIES: FileCategory[] = ['DOMAIN_INTRO', 'EVIDENCE', 'CERTIFICATE', 'FINANCE', 'ETC']

const PARSE_STATUS_LABELS: Record<CompanyFile['parseStatus'], string> = {
  PENDING: '분석 중',
  SUCCESS: '분석 완료',
  FAILED: '분석 실패',
}

export default function CompanyFilesPage() {
  const [activeCategory, setActiveCategory] = useState<FileCategory | undefined>(undefined)
  const [files, setFiles] = useState<CompanyFile[]>([])
  const [isUploading, setIsUploading] = useState(false)

  async function refresh() {
    setFiles(await listCompanyFiles(activeCategory))
  }

  useEffect(() => {
    refresh()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeCategory])

  async function handleFilesSelected(selected: File[]) {
    if (!activeCategory) return
    setIsUploading(true)
    try {
      for (const file of selected) {
        await uploadCompanyFile(file, activeCategory)
      }
      await refresh()
    } finally {
      setIsUploading(false)
    }
  }

  async function handleDelete(id: number) {
    await deleteCompanyFile(id)
    await refresh()
  }

  return (
    <div>
      <p className="text-xs font-medium tracking-[0.4em] text-muted">자료실</p>
      <h1 className="mt-4 text-4xl font-semibold tracking-tight text-offwhite">회사 자료실</h1>

      <div className="mt-10 grid gap-8 md:grid-cols-[160px_1fr]">
        <nav className="flex gap-2 overflow-x-auto md:flex-col">
          <button
            onClick={() => setActiveCategory(undefined)}
            className={`whitespace-nowrap rounded px-3 py-2 text-left text-sm ${
              activeCategory === undefined ? 'text-accent' : 'text-muted hover:text-offwhite'
            }`}
          >
            전체
          </button>
          {CATEGORIES.map((category) => (
            <button
              key={category}
              onClick={() => setActiveCategory(category)}
              className={`whitespace-nowrap rounded px-3 py-2 text-left text-sm ${
                activeCategory === category ? 'text-accent' : 'text-muted hover:text-offwhite'
              }`}
            >
              {CATEGORY_LABELS[category]}
            </button>
          ))}
        </nav>

        <div>
          {activeCategory ? (
            <>
              <div className="flex items-center gap-2">
                <p className="text-sm text-offwhite">
                  <span className="text-accent">{CATEGORY_LABELS[activeCategory]}</span>
                </p>
                {isUploading && <span className="text-xs text-muted">업로드 중…</span>}
              </div>
              <div className="mt-4">
                <FileUploader onFilesSelected={handleFilesSelected} accept=".docx,.pdf,.hwp,.hwpx" />
              </div>
            </>
          ) : (
            <p className="rounded-lg border border-dashed border-hairline px-6 py-8 text-center text-sm text-muted">
              업로드하려면 왼쪽에서 카테고리를 먼저 선택하세요.
            </p>
          )}

          <table className="mt-8 w-full text-left text-sm">
            <thead className="text-xs uppercase tracking-wide text-muted">
              <tr className="border-b border-hairline">
                <th className="py-3 font-normal">파일명</th>
                <th className="py-3 font-normal">카테고리</th>
                <th className="py-3 font-normal">업로드일</th>
                <th className="py-3 font-normal">상태</th>
                <th className="py-3 font-normal"></th>
              </tr>
            </thead>
            <tbody>
              {files.map((file) => (
                <tr key={file.id} className="border-b border-hairline/60">
                  <td className="py-3 text-offwhite">{file.fileName}</td>
                  <td className="py-3 text-muted">{CATEGORY_LABELS[file.category]}</td>
                  <td className="py-3 text-muted">
                    {new Date(file.uploadedAt).toLocaleDateString('ko-KR')}
                  </td>
                  <td className="py-3 text-muted">{PARSE_STATUS_LABELS[file.parseStatus]}</td>
                  <td className="py-3 text-right">
                    <button
                      onClick={() => handleDelete(file.id)}
                      className="text-muted hover:text-offwhite"
                    >
                      삭제
                    </button>
                  </td>
                </tr>
              ))}
              {files.length === 0 && (
                <tr>
                  <td colSpan={5} className="py-8 text-center text-muted">
                    등록된 자료가 없습니다.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
