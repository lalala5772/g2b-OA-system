import { googleLoginUrl } from '../api/auth'

export default function LoginPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-navy-950 px-6 text-center">
      <div
        className="pointer-events-none absolute inset-0 opacity-40"
        style={{
          background:
            'radial-gradient(ellipse at 50% 0%, rgba(79,140,255,0.16), transparent 60%)',
        }}
      />
      <p className="relative text-xs font-medium tracking-[0.4em] text-muted">G2B AUTOMATION</p>
      <h1 className="relative mt-4 text-4xl font-semibold tracking-tight text-offwhite md:text-5xl">
        업무를 연결하고,
        <br />
        자동화하는 시스템
      </h1>
      <p className="relative mt-6 max-w-md text-sm leading-relaxed text-muted">
        나라장터 공고 탐지부터 문서 작성, 증빙자료 정리까지 —
        한 곳에서 관리합니다.
      </p>
      <a
        href={googleLoginUrl()}
        className="relative mt-12 rounded-full border border-hairline bg-navy-900 px-8 py-3 text-sm text-offwhite transition-colors hover:border-accent hover:text-accent"
      >
        Sign in with Google
      </a>
    </div>
  )
}
