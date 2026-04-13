import { useState } from 'react';

const MONTH_ABBR = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

/**
 * Bar chart for 6-month spending trend. (T106)
 * Always renders exactly 6 bars. Zero-spend months render a zero-height bar.
 * In-progress (isComplete=false) months have a visual indicator.
 */
const SixMonthBarChart = ({ sixMonthTrend = [] }) => {
  const [hovered, setHovered] = useState(null);

  const maxAmount = Math.max(...sixMonthTrend.map((m) => m.totalSpend || 0), 1);

  const W = 520, H = 200;
  const PAD = { l: 58, r: 20, t: 36, b: 54 };
  const chartW = W - PAD.l - PAD.r;
  const chartH = H - PAD.t - PAD.b;
  const n = sixMonthTrend.length || 1;
  const slot = chartW / n;
  const barW = Math.min(slot * 0.52, 52);

  const fmtY = (v) => v >= 1000 ? `$${(v / 1000).toFixed(0)}k` : `$${v.toFixed(0)}`;
  const fmtFull = (v) =>
    v.toLocaleString('en-CA', { style: 'currency', currency: 'CAD', minimumFractionDigits: 2 });

  return (
    <div className="bar-chart-wrap">
      <svg
        viewBox={`0 0 ${W} ${H}`}
        style={{ width: '100%', height: 'auto', display: 'block', overflow: 'visible' }}
      >
        <defs>
          <linearGradient id="barGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#6366f1" />
            <stop offset="100%" stopColor="#a5b4fc" />
          </linearGradient>
          <linearGradient id="barGradHov" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#4338ca" />
            <stop offset="100%" stopColor="#818cf8" />
          </linearGradient>
          <linearGradient id="barGradPartial" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#93c5fd" />
            <stop offset="100%" stopColor="#bfdbfe" />
          </linearGradient>
        </defs>

        {/* Y-axis gridlines + labels */}
        {[0, 0.25, 0.5, 0.75, 1].map((frac) => {
          const y = PAD.t + chartH * (1 - frac);
          return (
            <g key={frac}>
              <line
                x1={PAD.l} y1={y} x2={W - PAD.r} y2={y}
                stroke={frac === 0 ? '#cbd5e1' : '#e2e8f0'}
                strokeWidth="1"
                strokeDasharray={frac === 0 ? '' : '4 3'}
              />
              <text x={PAD.l - 6} y={y + 4} textAnchor="end" fontSize="9.5" fill="#94a3b8">
                {fmtY(maxAmount * frac)}
              </text>
            </g>
          );
        })}

        {/* Bars */}
        {sixMonthTrend.map((m, i) => {
          const amt = m.totalSpend || 0;
          const barH = Math.max((amt / maxAmount) * chartH, amt > 0 ? 4 : 0);
          const cx = PAD.l + i * slot + slot / 2;
          const x = cx - barW / 2;
          const y = PAD.t + chartH - barH;
          const isHov = hovered === i;
          const isPartial = m.complete === false;
          const gradId = isHov ? 'url(#barGradHov)' : isPartial ? 'url(#barGradPartial)' : 'url(#barGrad)';
          const label = MONTH_ABBR[(m.month - 1) % 12];

          return (
            <g
              key={i}
              onMouseEnter={() => setHovered(i)}
              onMouseLeave={() => setHovered(null)}
              style={{ cursor: 'pointer' }}
            >
              {/* Invisible wide hover hit zone */}
              <rect x={cx - slot / 2 + 2} y={0} width={slot - 4} height={H} fill="transparent" />

              {/* Bar */}
              <rect
                x={x} y={y} width={barW} height={barH}
                rx="5" ry="5"
                fill={gradId}
                style={{ transition: 'opacity .15s' }}
                opacity={hovered !== null && !isHov ? 0.45 : 1}
              />

              {/* Amount label above bar */}
              {amt > 0 && (
                <text
                  x={cx} y={y - 6}
                  textAnchor="middle" fontSize="10" fontWeight="600"
                  fill={isHov ? '#4338ca' : '#475569'}
                >
                  {fmtY(amt)}
                </text>
              )}

              {/* In-progress amber dot */}
              {isPartial && (
                <circle cx={cx} cy={y - 17} r="3.5" fill="#f59e0b" />
              )}

              {/* Month label */}
              <text
                x={cx} y={PAD.t + chartH + 18}
                textAnchor="middle" fontSize="12"
                fontWeight={isHov ? '700' : '500'}
                fill={isHov ? '#4338ca' : '#64748b'}
              >
                {label}
              </text>

              {/* Year label */}
              <text x={cx} y={PAD.t + chartH + 34} textAnchor="middle" fontSize="9.5" fill="#94a3b8">
                {m.year}
              </text>
            </g>
          );
        })}
      </svg>

      {/* Hover tooltip */}
      {hovered !== null && sixMonthTrend[hovered] && (
        <div className="bar-tooltip">
          <span className="bar-tooltip-month">
            {MONTH_ABBR[(sixMonthTrend[hovered].month - 1) % 12]} {sixMonthTrend[hovered].year}
          </span>
          <span className="bar-tooltip-amount">
            {fmtFull(sixMonthTrend[hovered].totalSpend || 0)}
          </span>
          {sixMonthTrend[hovered].complete === false && (
            <span className="bar-tooltip-partial">In progress</span>
          )}
        </div>
      )}

      {/* Bottom legend */}
      <div className="bar-chart-legend">
        <span className="bar-legend-dot" style={{ backgroundColor: '#6366f1' }} />
        <span>Completed</span>
        <span className="bar-legend-dot" style={{ backgroundColor: '#93c5fd' }} />
        <span>In progress</span>
      </div>
    </div>
  );
};

export default SixMonthBarChart;
