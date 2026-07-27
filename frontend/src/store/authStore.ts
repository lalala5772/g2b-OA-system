import { create } from 'zustand'
import { fetchCurrentUser, logout as apiLogout, type CurrentUser } from '../api/auth'

interface AuthState {
  /** undefined = not checked yet, null = checked and logged out */
  user: CurrentUser | null | undefined
  fetchUser: () => Promise<void>
  logout: () => Promise<void>
}

export const useAuthStore = create<AuthState>((set) => ({
  user: undefined,
  fetchUser: async () => {
    const user = await fetchCurrentUser()
    set({ user })
  },
  logout: async () => {
    await apiLogout()
    set({ user: null })
  },
}))
