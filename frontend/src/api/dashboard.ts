import { apiClient, type ApiResponse } from './client'

export interface FeatureStatus {
  status: string
  badge: string
}

export interface DashboardSummary {
  totalCompanyFiles: number
  companyFilesByCategory: Record<string, number>
  idea: FeatureStatus
  document: FeatureStatus
  evidence: FeatureStatus
  bid: FeatureStatus
}

export async function fetchDashboardSummary(): Promise<DashboardSummary> {
  const { data } = await apiClient.get<ApiResponse<DashboardSummary>>('/api/dashboard/summary')
  return data.data
}
