const CATEGORY_COLORS = {
  'Housing': '#005f73',
  'Transport': '#9b2226',
  'Food & Drink': '#0a9396',
  'Entertainment': '#ca6702',
  'Shopping': '#3a86ff',
  'Utilities': '#ff006e',
  'Health': '#6d597a',
  'Income': '#2b9348',
  'Other': '#ee9b00',
  'No category': '#111827'
};

function toAmount(value) {
  const amount = Number.parseFloat(value ?? 0);
  return Number.isFinite(amount) ? amount : 0;
}

function monthLabel(entry) {
  return `${entry.year}-${String(entry.month).padStart(2, '0')}`;
}

export function SpendingBarChart({ entries }) {
  if (!Array.isArray(entries) || entries.length === 0) {
    return (
      <div className="chart-card empty-state stack tight-gap">
        <h3>Six-Month Trend</h3>
        <p className="muted">No trend data returned for this period yet.</p>
      </div>
    );
  }

  const amounts = entries.map((entry) => toAmount(entry.totalSpend ?? entry.totalDebitSpend));
  const maxAmount = Math.max(...amounts, 0);

  return (
    <div className="chart-card stack">
      <h3>Six-Month Trend</h3>
      <div className="bar-chart">
        {entries.map((entry) => {
          const amount = toAmount(entry.totalSpend ?? entry.totalDebitSpend);
          const height = maxAmount > 0 ? `${(amount / maxAmount) * 100}%` : '0%';

          return (
            <div className="bar-row" key={`${entry.year}-${entry.month}`}>
              <div className="bar-track">
                <div className="bar-fill" style={{ height }} />
              </div>
              <div className="bar-meta">
                <strong>{monthLabel(entry)}</strong>
                <span className="muted compact-text">{amount.toFixed(2)}</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export function SpendingPieChart({ categories }) {
  if (!Array.isArray(categories) || categories.length === 0) {
    return (
      <div className="chart-card empty-state stack tight-gap">
        <h3>Category Breakdown</h3>
        <p className="muted">No category data returned for this month yet.</p>
      </div>
    );
  }

  const totalAmount = categories.reduce((sum, category) => sum + toAmount(category.totalAmount), 0);
  const isEmpty = totalAmount <= 0;
  const visibleCategories = categories.filter((category) => toAmount(category.totalAmount) > 0);

  let runningTotal = 0;
  const segments = [];

  visibleCategories.forEach((category, index) => {
    const percentage = totalAmount > 0 ? (toAmount(category.totalAmount) / totalAmount) * 100 : 0;
    const start = runningTotal;
    runningTotal = Math.min(100, runningTotal + percentage);
    const end = index === visibleCategories.length - 1 ? 100 : runningTotal;
    const color = CATEGORY_COLORS[category.category] || '#475569';

    if (end > start) {
      segments.push(`${color} ${start}% ${end}%`);
    }
  });

  const chartStyle = isEmpty
    ? { background: 'conic-gradient(#d8d1c7 0 100%)' }
    : { background: `conic-gradient(${segments.join(', ')})` };

  return (
    <div className="chart-card stack">
      <h3>Category Breakdown</h3>
      <div className="pie-layout">
        <div className={`pie-chart${isEmpty ? ' empty' : ''}`} style={chartStyle} aria-hidden="true">
          {isEmpty ? <span className="pie-empty-label">No spend</span> : null}
        </div>
        <div className="pie-legend">
          {categories.map((category, index) => (
            <div className="legend-row" key={`${category.category}-${index}`}>
              <span className="legend-swatch" style={{ backgroundColor: CATEGORY_COLORS[category.category] || '#475569' }} />
              <div>
                <strong>{category.category}</strong>
                <p className="muted compact-text">{category.totalAmount} ({category.percentage}%)</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}