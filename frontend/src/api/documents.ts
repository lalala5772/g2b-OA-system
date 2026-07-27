import { apiClient, type ApiResponse } from './client'

export interface DocumentAutoFillResult {
  id: number
  status: 'SUCCESS' | 'FAILED'
  filledFields: Record<string, string>
  createdAt: string
}

export async function autoFillDocument(file: File): Promise<DocumentAutoFillResult> {
  const formData = new FormData()
  formData.append('file', file)
  const { data } = await apiClient.post<ApiResponse<DocumentAutoFillResult>>('/api/documents/auto-fill', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data.data
}

export function downloadUrl(generationId: number): string {
  const base = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
  return `${base}/api/documents/generations/${generationId}/download`
}
