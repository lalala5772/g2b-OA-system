import { useEffect, useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { fetchCurrentUser, logout, type CurrentUser } from '../api/auth'

const NAV_ITEMS = [
  { to: '/ideas', label: '아이디어' },
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
          <NavLink to="/dashboard" className="text-sm font-semibold tracking-[0.2em] text-offwhite">
            G2B · OA SYSTEM
          </NavLink>
          <nav className="flex items-center gap-6 text-sm text-muted">
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
          <div className="flex items-center gap-3">
            {user && (
              <>
                {user.profileImageUrl && (
                  <img src={user.profileImageUrl} alt="" className="h-7 w-7 rounded-full" />
                )}
                <span className="text-sm text-muted">{user.name}</span>
                <button
                  onClick={handleLogout}
                  className="text-sm text-muted transition-colors hover:text-offwhite"
                >
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
