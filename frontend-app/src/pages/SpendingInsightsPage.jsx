import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { mapAxiosError } from '../api/axiosClient';
import { SpendingBarChart, SpendingPieChart } from '../components/InsightCharts';
import { useSpendingInsights } from '../hooks/useGroup3';
import { emptySpendingInsightsLookup } from '../types';

const MONTH_OPTIONS = [
  { value: '01', label: 'January' },
  { value: '02', label: 'February' },
  { value: '03', label: 'March' },
  { value: '04', label: 'April' },
  { value: '05', label: 'May' },
  { value: '06', label: 'June' },
  { value: '07', label: 'July' },
  { value: '08', label: 'August' },
  { value: '09', label: 'September' },
  { value: '10', label: 'October' },
  { value: '11', label: 'November' },
  { value: '12', label: 'December' }
];

function splitPeriod(period) {
  const [year = '', month = ''] = String(period || '').split('-');
  return { year, month };
}

function joinPeriod(year, month) {
  if (!year || !month) {
    return '';
  }

  return `${year}-${month}`;
}

const MOCK_SPENDING_INSIGHTS = {
  totalDebitSpend: '624.75',
  transactionCount: 12,
  dataFresh: false,
  hasUncategorised: true,
  hasExcludedDisputes: false,
  sixMonthTrend: [
    { year: 2025, month: 11, totalDebitSpend: '410.20' },
    { year: 2025, month: 12, totalDebitSpend: '455.90' },
    { year: 2026, month: 1, totalDebitSpend: '498.35' },
    { year: 2026, month: 2, totalDebitSpend: '552.10' },
    { year: 2026, month: 3, totalDebitSpend: '624.75' },
    { year: 2026, month: 4, totalDebitSpend: '571.40' }
  ],
  categoryBreakdown: [
    { category: 'Food & Drink', totalAmount: '210.00', percentage: '33.61' },
    { category: 'Bills & Utilities', totalAmount: '155.25', percentage: '24.85' },
    { category: 'Transport', totalAmount: '92.50', percentage: '14.81' },
    { category: 'Shopping', totalAmount: '101.00', percentage: '16.17' },
    { category: 'Uncategorised', totalAmount: '66.00', percentage: '10.56' }
  ]
};

export function SpendingInsightsPage() {
  const { accountId } = useParams();
  const initial = splitPeriod(emptySpendingInsightsLookup.period);
  const [form, setForm] = useState({ year: initial.year, month: initial.month });
  const [submittedPeriod, setSubmittedPeriod] = useState('');
  const query = useSpendingInsights({ accountId, period: submittedPeriod });

  function handleSubmit(event) {
    event.preventDefault();
    const period = joinPeriod(form.year, form.month);
    if (!period) {
      return;
    }

    setSubmittedPeriod(period);
  }

  const insights = query.data;
  const chartData = insights || MOCK_SPENDING_INSIGHTS;
  const queryError = query.error ? mapAxiosError(query.error) : null;

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">GET /accounts/{'{accountId}'}/insights</p>
          <h2>Spending Insights</h2>
          <p className="muted">Review debit-spend totals for a selected month as a pie chart plus a six-month bar chart trend.</p>
        </div>
        <div className="banner info">This page is scaffolded against the future Group 3 backend contract. Until that merge lands, live requests from this screen may fail.</div>
        <form className="form-grid" onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="insights-year">Insight Year</label>
            <input
              id="insights-year"
              type="number"
              min="1900"
              max="9999"
              step="1"
              value={form.year}
              onChange={(event) => setForm((current) => ({ ...current, year: event.target.value }))}
              placeholder="e.g. 2026"
              required
            />
          </div>
          <div className="field">
            <label htmlFor="insights-month">Insight Month</label>
            <select
              id="insights-month"
              value={form.month}
              onChange={(event) => setForm((current) => ({ ...current, month: event.target.value }))}
              required
            >
              <option value="">Select month</option>
              {MONTH_OPTIONS.map((month) => (
                <option key={month.value} value={month.value}>{month.label}</option>
              ))}
            </select>
          </div>
          <div className="actions">
            <button type="submit" disabled={query.isFetching}>Load Insights</button>
            <Link className="button-link subtle" to={`/accounts/${accountId}`}>Back to Account</Link>
          </div>
        </form>
        {query.isLoading || query.isFetching ? <div className="banner success">Loading spending insights...</div> : null}
        {queryError ? <div className="banner error">{queryError.message}</div> : null}
        {!submittedPeriod ? <div className="banner info">Pick a month and click Load Insights to request backend data.</div> : null}
        {!insights ? <div className="banner info">Showing mock preview data so you can review the chart layout before Group 3 backend integration is available.</div> : null}
      </section>

      <section className="panel stack">
        <div className="card-grid">
          <article className="metric">
            <p className="muted">Selected Period</p>
            <strong>{submittedPeriod || 'None'}</strong>
          </article>
          <article className="metric">
            <p className="muted">Total Debit Spend</p>
            <strong>{chartData.totalDebitSpend ?? '0.00'}</strong>
          </article>
          <article className="metric">
            <p className="muted">Transactions Counted</p>
            <strong>{chartData.transactionCount ?? 0}</strong>
          </article>
          <article className="metric">
            <p className="muted">Data Fresh</p>
            <strong>{chartData.dataFresh === false ? 'No' : 'Yes'}</strong>
          </article>
        </div>
        <div className="chart-grid">
          <SpendingBarChart entries={chartData.sixMonthTrend} />
          <SpendingPieChart categories={chartData.categoryBreakdown} />
        </div>
        <div className="detail-grid">
          <article className="detail-item">
            <p className="muted">Uncategorised Transactions</p>
            <strong>{chartData.hasUncategorised ? 'Yes' : 'No'}</strong>
          </article>
          <article className="detail-item">
            <p className="muted">Excluded Disputes</p>
            <strong>{chartData.hasExcludedDisputes ? 'Yes' : 'No'}</strong>
          </article>
        </div>
      </section>
    </div>
  );
}