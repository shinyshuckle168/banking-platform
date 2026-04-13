import { useState } from 'react';

const COLOURS = [
  '#6366f1', // indigo
  '#f59e0b', // amber
  '#10b981', // emerald
  '#ef4444', // red
  '#8b5cf6', // violet
  '#3b82f6', // blue
  '#ec4899', // pink
  '#14b8a6', // teal
];

/**
 * SVG donut chart for 8 spending categories. (T105)
 * All 8 segments always rendered, including zero-value segments.
 */
const CategoryDonutChart = ({ categoryBreakdown = [] }) => {
  const [hovered, setHovered] = useState(null);

  const total = categoryBreakdown.reduce((sum, c) => sum + (c.percentage || 0), 0);

  const RADIUS = 78;
  const STROKE = 24;
  const CIRCUMFERENCE = 2 * Math.PI * RADIUS;
  const CENTER = 108;

  let offset = 0;
  const segments = categoryBreakdown.map((cat, i) => {
    const pct = total > 0 ? cat.percentage / total : 0;
    const rawDash = pct * CIRCUMFERENCE;
    const dashLen = Math.max(rawDash - 3, 0); // 3px gap between segments
    const gapLen = CIRCUMFERENCE - dashLen;
    const seg = { ...cat, dashLen, gapLen, offset, colour: COLOURS[i % COLOURS.length] };
    offset += rawDash;
    return seg;
  });

  const hovSeg = hovered !== null ? segments[hovered] : null;

  return (
    <div className="donut-wrap">
      {/* SVG Donut */}
      <div className="donut-svg-col">
        <svg
          width={CENTER * 2}
          height={CENTER * 2}
          viewBox={`0 0 ${CENTER * 2} ${CENTER * 2}`}
          aria-label="Category breakdown donut chart"
        >
          {/* Background track */}
          <circle cx={CENTER} cy={CENTER} r={RADIUS} fill="none" stroke="#f1f5f9" strokeWidth={STROKE} />

          {/* Segments */}
          {segments.map((seg, i) => (
            <circle
              key={seg.category}
              cx={CENTER}
              cy={CENTER}
              r={RADIUS}
              fill="none"
              stroke={seg.colour}
              strokeWidth={hovered === i ? STROKE + 5 : STROKE}
              strokeDasharray={`${seg.dashLen} ${seg.gapLen}`}
              strokeDashoffset={-seg.offset}
              transform={`rotate(-90 ${CENTER} ${CENTER})`}
              style={{
                cursor: 'pointer',
                transition: 'stroke-width .2s, opacity .2s',
                opacity: hovered === null || hovered === i ? 1 : 0.25,
              }}
              onMouseEnter={() => setHovered(i)}
              onMouseLeave={() => setHovered(null)}
            />
          ))}

          {/* Center label */}
          {hovSeg ? (
            <>
              <text x={CENTER} y={CENTER - 14} textAnchor="middle" fontSize="10" fontWeight="600" fill="#94a3b8">
                {hovSeg.category.split(/[\s&]/)[0].toUpperCase()}
              </text>
              <text x={CENTER} y={CENTER + 13} textAnchor="middle" fontSize="26" fontWeight="800" fill="#0f172a">
                {(hovSeg.percentage || 0).toFixed(1)}%
              </text>
            </>
          ) : (
            <>
              <text x={CENTER} y={CENTER - 10} textAnchor="middle" fontSize="10" fontWeight="600" fill="#94a3b8" letterSpacing="1">
                TOTAL
              </text>
              <text x={CENTER} y={CENTER + 14} textAnchor="middle" fontSize="26" fontWeight="800" fill="#0f172a">
                {total.toFixed(0)}%
              </text>
            </>
          )}
        </svg>
      </div>

      {/* Legend */}
      <ul className="donut-legend">
        {categoryBreakdown.map((cat, i) => (
          <li
            key={cat.category}
            className={`donut-legend-item${hovered === i ? ' donut-legend-item--active' : ''}`}
            onMouseEnter={() => setHovered(i)}
            onMouseLeave={() => setHovered(null)}
          >
            <span className="donut-swatch" style={{ background: COLOURS[i % COLOURS.length] }} />
            <span className="donut-cat">{cat.category}</span>
            <div className="donut-bar-track">
              <div
                className="donut-bar-fill"
                style={{ width: `${Math.min(cat.percentage || 0, 100)}%`, background: COLOURS[i % COLOURS.length] }}
              />
            </div>
            <span className="donut-pct">{(cat.percentage || 0).toFixed(1)}%</span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default CategoryDonutChart;
