import { apiClient, type ApiResponse } from './client'

export interface CurrentUser {
  id: number
  email: string
  name: string
  profileImageUrl: string | null
  role: string
}

export async function fetchCurrentUser(): Promise<CurrentUser | null> {
  try {
    const { data } = await apiClient.get<ApiResponse<CurrentUser>>('/api/auth/me')
    return data.data
  } catch {
    return null
  }
}

export async function logout(): Promise<void> {
  await apiClient.post('/api/auth/logout')
}

export function googleLoginUrl(): string {
  const base = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
  return `${base}/oauth2/authorization/google`
}
