import { apiClient, type ApiResponse } from './client'

export type FilePurpose = 'CONTEST' | 'REQUIREMENT_DOC'

export interface UploadedFile {
  id: number
  originalName: string
  fileType: string
  purpose: FilePurpose
  parseStatus: 'PENDING' | 'SUCCESS' | 'FAILED'
  extractedText: string | null
  uploadedAt: string
}

export async function uploadFile(file: File, purpose: FilePurpose): Promise<UploadedFile> {
  const formData = new FormData()
  formData.append('file', file)
  const { data } = await apiClient.post<ApiResponse<UploadedFile>>(
    `/api/files/upload?purpose=${purpose}`,
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  )
  return data.data
}
