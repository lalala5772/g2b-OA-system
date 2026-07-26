import { apiClient, type ApiResponse } from './client'

export interface DocumentField {
  key: string
  label: string
  auto: boolean
}

export interface DocumentTemplate {
  id: number
  name: string
  fields: DocumentField[]
  createdAt: string
}

export interface DocumentGenerationResult {
  id: number
  status: 'SUCCESS' | 'FAILED'
  autoFilledFields: Record<string, string>
  createdAt: string
}

export async function listTemplates(): Promise<DocumentTemplate[]> {
  const { data } = await apiClient.get<ApiResponse<DocumentTemplate[]>>('/api/documents/templates')
  return data.data
}

export async function uploadTemplate(file: File, name: string, fieldsSchema: DocumentField[]): Promise<DocumentTemplate> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('name', name)
  formData.append('fieldsSchema', JSON.stringify(fieldsSchema))
  const { data } = await apiClient.post<ApiResponse<DocumentTemplate>>('/api/documents/templates', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data.data
}

export async function generateDocument(
  templateId: number,
  fieldValues: Record<string, string>,
): Promise<DocumentGenerationResult> {
  const { data } = await apiClient.post<ApiResponse<DocumentGenerationResult>>('/api/documents/generate', {
    templateId,
    fieldValues,
  })
  return data.data
}

export function downloadUrl(generationId: number): string {
  const base = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
  return `${base}/api/documents/generations/${generationId}/download`
}
