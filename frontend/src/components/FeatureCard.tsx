import { Link } from 'react-router-dom'

interface FeatureCardProps {
  to: string
  title: string
  description: string
  badge?: string
}

export default function FeatureCard({ to, title, description, badge }: FeatureCardProps) {
  return (
    <Link
      to={to}
      className="btn-premium group relative flex flex-col justify-between rounded-lg border border-hairline bg-navy-900 p-8 hover:border-accent"
    >
      {badge && (
        <span className="absolute right-6 top-6 rounded-full bg-accent-soft px-3 py-1 text-xs text-accent">
          {badge}
        </span>
      )}
      <div>
        <h3 className="text-2xl font-semibold text-offwhite">{title}</h3>
        <p className="mt-3 text-sm leading-relaxed text-muted">{description}</p>
      </div>
      <span className="mt-8 text-sm text-muted transition-colors group-hover:text-accent">
        Access —
      </span>
    </Link>
  )
}
