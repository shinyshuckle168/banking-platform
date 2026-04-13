const PIE_COLORS = ['#046c4e', '#ad6a3b', '#1a759f', '#c1666b', '#ffb703', '#6c757d', '#588157', '#b56576'];

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
        <p className="muted">The future backend has not returned trend data for this period yet.</p>
      </div>
    );
  }

  const amounts = entries.map((entry) => toAmount(entry.totalDebitSpend));
  const maxAmount = Math.max(...amounts, 0);

  return (
    <div className="chart-card stack">
      <h3>Six-Month Trend</h3>
      <div className="bar-chart">
        {entries.map((entry) => {
          const amount = toAmount(entry.totalDebitSpend);
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
        <p className="muted">The future backend has not returned category data for this month yet.</p>
      </div>
    );
  }

  let runningTotal = 0;
  const segments = categories.map((category, index) => {
    const percentage = toAmount(category.percentage);
    const start = runningTotal;
    runningTotal += percentage;
    const end = runningTotal;
    return `${PIE_COLORS[index % PIE_COLORS.length]} ${start}% ${end}%`;
  });

  const chartStyle = {
    background: `conic-gradient(${segments.join(', ')})`
  };

  return (
    <div className="chart-card stack">
      <h3>Category Breakdown</h3>
      <div className="pie-layout">
        <div className="pie-chart" style={chartStyle} aria-hidden="true" />
        <div className="pie-legend">
          {categories.map((category, index) => (
            <div className="legend-row" key={category.category}>
              <span className="legend-swatch" style={{ backgroundColor: PIE_COLORS[index % PIE_COLORS.length] }} />
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