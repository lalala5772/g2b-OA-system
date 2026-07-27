import { apiClient, type ApiResponse } from './client'

export const MAX_KEYWORDS = 15

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
  aiSummary: string | null
  aiJudgement: string | null
  status: 'ELIGIBLE' | 'INELIGIBLE'
  crawledAt: string
}

export interface BidScanSummary {
  fetched: number
  matched: number
  newNotices: number
  eligibleCount: number
  judged: number
  unjudged: number
  rangeStart: string
  rangeEnd: string
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

/** 조회기간 내 적격 공고 (최대 50건, 최신순). 둘 다 비우면 서버 기본값(최근 7일) 사용 */
export async function fetchRecentEligible(startDate?: string, endDate?: string): Promise<BidNotice[]> {
  const params = startDate && endDate ? { startDate, endDate } : {}
  const { data } = await apiClient.get<ApiResponse<BidNotice[]>>('/api/bids/eligible', { params })
  return data.data
}

export async function fetchBidDetail(id: number): Promise<BidNotice> {
  const { data } = await apiClient.get<ApiResponse<BidNotice>>(`/api/bids/${id}`)
  return data.data
}

/** startDate/endDate 둘 다 비우면 서버 기본값(최근 7일)을 사용 */
export async function scanNow(startDate?: string, endDate?: string): Promise<BidScanSummary> {
  const body = startDate && endDate ? { startDate, endDate } : {}
  const { data } = await apiClient.post<ApiResponse<BidScanSummary>>('/api/bids/scan-now', body)
  return data.data
}
