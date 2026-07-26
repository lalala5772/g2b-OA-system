import { apiClient, type ApiResponse } from './client'

export interface ContestIdea {
  id: number
  ideaTitle: string
  ideaContent: string
  relevanceScore: number | null
  generatedAt: string
}

export interface IdeaGenerateResult {
  requestId: number
  status: 'PROCESSING' | 'COMPLETED' | 'FAILED'
  ideas: ContestIdea[]
}

export async function listRecentIdeas(): Promise<ContestIdea[]> {
  const { data } = await apiClient.get<ApiResponse<ContestIdea[]>>('/api/ideas')
  return data.data
}

export async function generateIdeas(contestFileId: number): Promise<IdeaGenerateResult> {
  const { data } = await apiClient.post<ApiResponse<IdeaGenerateResult>>('/api/ideas/generate', { contestFileId })
  return data.data
}
