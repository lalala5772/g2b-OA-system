import axios from 'axios'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  withCredentials: true,
})

export interface ApiResponse<T> {
  success: boolean
  data: T
  message: string | null
}
