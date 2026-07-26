import { useEffect, useRef } from 'react'

/**
 * Animated geospatial-radar background for the login hero — a slow lat/long
 * grid drift, range rings, a rotating sweep, and occasional "ping" blips
 * standing in for 공고 탐지 (a new bid notice being detected). Colors mirror
 * the CSS tokens in index.css (accent #d8c8a8 / offwhite #f3efe6) rather than
 * reading them at runtime, to keep the draw loop simple.
 */
export default function RadarField() {
  const canvasRef = useRef<HTMLCanvasElement>(null)

  useEffect(() => {
    const canvas = canvasRef.current
    const ctx = canvas?.getContext('2d')
    if (!canvas || !ctx) return

    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    const dpr = Math.min(window.devicePixelRatio || 1, 2)
    let width = 0
    let height = 0

    function resize() {
      if (!canvas || !ctx) return
      width = canvas.clientWidth
      height = canvas.clientHeight
      canvas.width = width * dpr
      canvas.height = height * dpr
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    }
    resize()
    window.addEventListener('resize', resize)

    const centerX = () => width * 0.8
    const centerY = () => height * 0.4
    const maxRadius = () => Math.min(width, height) * 0.55

    function drawGrid(t: number) {
      if (!ctx) return
      ctx.save()
      ctx.strokeStyle = 'rgba(243,239,230,0.05)'
      ctx.lineWidth = 1
      const spacing = 64
      const offset = (t * 0.006) % spacing
      for (let x = -spacing + offset; x < width + spacing; x += spacing) {
        ctx.beginPath()
        ctx.moveTo(x, 0)
        ctx.lineTo(x, height)
        ctx.stroke()
      }
      for (let y = 0; y < height; y += spacing) {
        ctx.beginPath()
        ctx.moveTo(0, y)
        ctx.lineTo(width, y)
        ctx.stroke()
      }
      ctx.restore()
    }

    function drawRings() {
      if (!ctx) return
      ctx.save()
      ctx.strokeStyle = 'rgba(216,200,168,0.14)'
      ctx.lineWidth = 1
      for (let i = 1; i <= 4; i++) {
        ctx.beginPath()
        ctx.arc(centerX(), centerY(), (maxRadius() / 4) * i, 0, Math.PI * 2)
        ctx.stroke()
      }
      ctx.restore()
    }

    function drawSweep(angle: number) {
      if (!ctx) return
      ctx.save()
      ctx.translate(centerX(), centerY())
      ctx.rotate(angle)
      const gradient = ctx.createLinearGradient(0, 0, maxRadius(), 0)
      gradient.addColorStop(0, 'rgba(216,200,168,0.32)')
      gradient.addColorStop(1, 'rgba(216,200,168,0)')
      ctx.beginPath()
      ctx.moveTo(0, 0)
      ctx.arc(0, 0, maxRadius(), -0.35, 0)
      ctx.closePath()
      ctx.fillStyle = gradient
      ctx.fill()
      ctx.restore()
    }

    interface Ping {
      angle: number
      radius: number
      bornAt: number
      lifeMs: number
    }
    let pings: Ping[] = []
    let lastPingAt = 0

    function maybeSpawnPing(t: number) {
      if (t - lastPingAt > 2200 + Math.random() * 1800) {
        lastPingAt = t
        pings.push({
          angle: Math.random() * Math.PI * 2,
          radius: maxRadius() * (0.25 + Math.random() * 0.7),
          bornAt: t,
          lifeMs: 1800,
        })
      }
    }

    function drawPings(t: number) {
      if (!ctx) return
      pings = pings.filter((p) => t - p.bornAt < p.lifeMs)
      for (const p of pings) {
        const age = (t - p.bornAt) / p.lifeMs
        const x = centerX() + Math.cos(p.angle) * p.radius
        const y = centerY() + Math.sin(p.angle) * p.radius
        const r = 3 + age * 14
        ctx.save()
        ctx.globalAlpha = 1 - age
        ctx.strokeStyle = 'rgba(243,239,230,0.8)'
        ctx.lineWidth = 1.5
        ctx.beginPath()
        ctx.arc(x, y, r, 0, Math.PI * 2)
        ctx.stroke()
        ctx.fillStyle = 'rgba(243,239,230,0.9)'
        ctx.beginPath()
        ctx.arc(x, y, 2, 0, Math.PI * 2)
        ctx.fill()
        ctx.restore()
      }
    }

    let raf = 0
    let angle = 0

    function frame(t: number) {
      if (!ctx) return
      ctx.clearRect(0, 0, width, height)
      drawGrid(t)
      drawRings()
      maybeSpawnPing(t)
      drawPings(t)
      drawSweep(angle)
      angle += 0.006
      raf = requestAnimationFrame(frame)
    }

    if (reduceMotion) {
      drawGrid(0)
      drawRings()
      drawSweep(0)
    } else {
      raf = requestAnimationFrame(frame)
    }

    return () => {
      cancelAnimationFrame(raf)
      window.removeEventListener('resize', resize)
    }
  }, [])

  return (
    <canvas ref={canvasRef} className="pointer-events-none absolute inset-0 h-full w-full" aria-hidden="true" />
  )
}
