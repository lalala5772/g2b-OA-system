import { googleLoginUrl } from '../api/auth'
import RadarField from '../components/RadarField'

export default function LoginPage() {
  return (
    <div className="relative flex min-h-screen flex-col items-center overflow-hidden bg-navy-950 px-6 pb-16 pt-36 text-center md:pt-44">
      <RadarField />
      <div
        className="pointer-events-none absolute inset-0"
        style={{
          background:
            'radial-gradient(ellipse 55% 45% at 30% 15%, rgba(165,118,46,0.10), transparent 60%), ' +
            'radial-gradient(ellipse 70% 60% at 85% 100%, rgba(165,118,46,0.08), transparent 60%)',
        }}
      />

      <span className="absolute left-8 top-8 font-serif text-xl tracking-wide text-offwhite md:left-12 md:top-10">
        G2B — Automation
      </span>

      <p className="relative text-xs font-medium tracking-[0.4em] text-muted">
        OA Program
      </p>

      <h1 className="relative mt-6 text-5xl font-semibold leading-[1.15] tracking-tight text-offwhite md:text-7xl">
        업무
        <br />
        자동화 시스템
      </h1>

      <p className="relative mt-8 text-xl text-accent md:text-2xl">(주)메타지오스파셜 인트라넷 프로그램</p>

      <p className="relative mt-6 max-w-md text-sm leading-relaxed text-muted">
        나라장터 공고 적격탐지부터 문서 작성, 증빙자료 정리까지
        <br />한 곳에서 관리합니다.
      </p>

      <a
        href={googleLoginUrl()}
        className="btn-premium relative mt-14 rounded-full border border-hairline bg-navy-900/70 px-9 py-3.5 text-sm tracking-wide text-offwhite backdrop-blur hover:border-accent hover:text-accent"
      >
        Sign in with Google
      </a>
    </div>
  )
}
