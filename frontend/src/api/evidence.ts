import { apiClient, type ApiResponse } from './client'

export interface RequiredItem {
  id: number
  itemName: string
  description: string | null
  matched: boolean
  confidenceScore: number | null
  matchedFileName: string | null
}

export interface EvidenceAnalysisResult {
  requirementSetId: number
  status: 'PROCESSING' | 'COMPLETED' | 'FAILED'
  items: RequiredItem[]
  zipExportId: number | null
  matchedCount: number
  missingCount: number
}

export async function analyzeRequirements(uploadedFileId: number): Promise<EvidenceAnalysisResult> {
  const { data } = await apiClient.post<ApiResponse<EvidenceAnalysisResult>>('/api/evidence/analyze', {
    uploadedFileId,
  })
  return data.data
}

export function zipDownloadUrl(zipExportId: number): string {
  const base = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
  return `${base}/api/evidence/exports/${zipExportId}/download`
}
