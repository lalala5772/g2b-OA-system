import { apiClient, type ApiResponse } from './client'

export type FileCategory = 'DOMAIN_INTRO' | 'EVIDENCE' | 'CERTIFICATE' | 'FINANCE' | 'ETC'
export type ParseStatus = 'PENDING' | 'SUCCESS' | 'FAILED'

export interface CompanyFile {
  id: number
  fileName: string
  fileType: string
  category: FileCategory
  parseStatus: ParseStatus
  uploadedAt: string
  uploadedByName: string
}

export const CATEGORY_LABELS: Record<FileCategory, string> = {
  DOMAIN_INTRO: '도메인소개',
  EVIDENCE: '증빙서류',
  CERTIFICATE: '인증/자격',
  FINANCE: '재무자료',
  ETC: '기타',
}

export async function listCompanyFiles(category?: FileCategory): Promise<CompanyFile[]> {
  const { data } = await apiClient.get<ApiResponse<CompanyFile[]>>('/api/company-files', {
    params: category ? { category } : undefined,
  })
  return data.data
}

export async function uploadCompanyFile(file: File, category: FileCategory): Promise<CompanyFile> {
  const formData = new FormData()
  formData.append('file', file)
  const { data } = await apiClient.post<ApiResponse<CompanyFile>>(
    `/api/company-files/upload?category=${category}`,
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  )
  return data.data
}

export async function deleteCompanyFile(id: number): Promise<void> {
  await apiClient.delete(`/api/company-files/${id}`)
}

export function companyFileDownloadUrl(id: number): string {
  const base = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
  return `${base}/api/company-files/${id}/download`
}
