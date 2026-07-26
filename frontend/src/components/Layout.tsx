import { useEffect, useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { fetchCurrentUser, logout, type CurrentUser } from '../api/auth'

const NAV_ITEMS = [
  { to: '/documents', label: '문서' },
  { to: '/evidence', label: '증빙' },
  { to: '/bids', label: '나라장터' },
  { to: '/company-files', label: '자료실' },
]

export default function Layout() {
  const [user, setUser] = useState<CurrentUser | null | undefined>(undefined)
  const navigate = useNavigate()

  useEffect(() => {
    fetchCurrentUser().then(setUser)
  }, [])

  useEffect(() => {
    if (user === null) {
      navigate('/', { replace: true })
    }
  }, [user, navigate])

  async function handleLogout() {
    await logout()
    setUser(null)
  }

  return (
    <div className="min-h-screen bg-navy-950 text-offwhite">
      <header className="sticky top-0 z-10 border-b border-hairline bg-navy-950/90 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <NavLink to="/dashboard" className="font-serif text-lg tracking-wide text-offwhite">
            G2B — Automation
          </NavLink>
          <nav className="flex items-center gap-8 text-sm text-muted">
            {NAV_ITEMS.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  isActive ? 'text-offwhite' : 'transition-colors hover:text-offwhite'
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
          <div className="flex items-center gap-2.5 text-sm text-muted">
            {user && (
              <>
                {user.profileImageUrl && (
                  <img src={user.profileImageUrl} alt="" className="mr-1 h-6 w-6 rounded-full" />
                )}
                <span>{user.name}</span>
                <span className="text-hairline">/</span>
                <button onClick={handleLogout} className="transition-colors hover:text-offwhite">
                  로그아웃
                </button>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-6 py-10">
        <Outlet />
      </main>
    </div>
  )
}
