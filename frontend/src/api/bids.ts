import { apiClient, type ApiResponse } from './client'

export interface BidKeyword {
  id: number
  keyword: string
  active: boolean
}

export interface BidNotice {
  id: number
  title: string
  agency: string | null
  matchedKeyword: string | null
  announceDate: string | null
  deadline: string | null
  url: string | null
  eligibilityScore: number | null
  aiJudgement: string | null
  status: 'ELIGIBLE' | 'INELIGIBLE'
  crawledAt: string
}

export interface BidWindow {
  windowStart: string
  windowEnd: string
  notices: BidNotice[]
}

export interface BidScanSummary {
  fetched: number
  newNotices: number
  eligibleCount: number
}

export async function listKeywords(): Promise<BidKeyword[]> {
  const { data } = await apiClient.get<ApiResponse<BidKeyword[]>>('/api/bids/keywords')
  return data.data
}

export async function addKeyword(keyword: string): Promise<BidKeyword> {
  const { data } = await apiClient.post<ApiResponse<BidKeyword>>('/api/bids/keywords', { keyword })
  return data.data
}

export async function removeKeyword(id: number): Promise<void> {
  await apiClient.delete(`/api/bids/keywords/${id}`)
}

/** 어제 오전 10시 ~ 오늘 오전 10시 사이 감지된 적격 공고 */
export async function fetchEligibleWindow(): Promise<BidWindow> {
  const { data } = await apiClient.get<ApiResponse<BidWindow>>('/api/bids/eligible')
  return data.data
}

export async function scanNow(): Promise<BidScanSummary> {
  const { data } = await apiClient.post<ApiResponse<BidScanSummary>>('/api/bids/scan-now')
  return data.data
}
