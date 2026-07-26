interface ComingSoonFeatureProps {
  title: string
  description: string
  formHint: string
}

export default function ComingSoonFeature({ title, description, formHint }: ComingSoonFeatureProps) {
  return (
    <div>
      <h1 className="text-3xl font-semibold text-offwhite">{title}</h1>
      <p className="mt-2 max-w-2xl text-sm text-muted">{description}</p>

      <div className="mt-10 grid gap-6 md:grid-cols-2">
        <section className="rounded-lg border border-hairline bg-navy-900 p-6">
          <h2 className="text-sm font-semibold text-offwhite">입력</h2>
          <p className="mt-4 text-sm text-muted">{formHint}</p>
        </section>

        <section className="flex items-center justify-center rounded-lg border border-hairline bg-navy-900 p-6">
          <p className="animate-pulse text-sm text-muted">— Phase 2+ 제공 예정 —</p>
        </section>
      </div>
    </div>
  )
}
