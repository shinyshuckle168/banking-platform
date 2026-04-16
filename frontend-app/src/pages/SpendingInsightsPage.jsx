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

const INSIGHT_CATEGORIES = [
  'Housing',
  'Transport',
  'Food & Drink',
  'Entertainment',
  'Shopping',
  'Utilities',
  'Health',
  'Income'
];

function parsePeriod(period) {
  const [yearText, monthText] = String(period || '').split('-');
  const year = Number.parseInt(yearText, 10);
  const month = Number.parseInt(monthText, 10);

  if (!Number.isFinite(year) || !Number.isFinite(month) || month < 1 || month > 12) {
    const now = new Date();
    return { year: now.getFullYear(), month: now.getMonth() + 1 };
  }

  return { year, month };
}

function buildEmptySixMonthTrend(period) {
  const { year, month } = parsePeriod(period);
  const current = new Date();
  const currentYear = current.getFullYear();
  const currentMonth = current.getMonth() + 1;
  const entries = [];

  for (let offset = 5; offset >= 0; offset -= 1) {
    const date = new Date(Date.UTC(year, month - 1, 1));
    date.setUTCMonth(date.getUTCMonth() - offset);

    const entryYear = date.getUTCFullYear();
    const entryMonth = date.getUTCMonth() + 1;
    const isComplete = entryYear < currentYear || (entryYear === currentYear && entryMonth < currentMonth);

    entries.push({
      year: entryYear,
      month: entryMonth,
      totalSpend: '0.00',
      isComplete,
      accountExisted: true
    });
  }

  return entries;
}

function buildEmptyBreakdown() {
  return INSIGHT_CATEGORIES.map((category) => ({
    category,
    totalAmount: '0.00',
    percentage: '0.00',
    transactionCount: 0
  }));
}

function normalizeInsights(data, period) {
  const empty = {
    totalDebitSpend: '0.00',
    transactionCount: 0,
    hasUncategorised: false,
    hasExcludedDisputes: false,
    dataFresh: true,
    categoryBreakdown: buildEmptyBreakdown(),
    sixMonthTrend: buildEmptySixMonthTrend(period)
  };

  if (!data) {
    return empty;
  }

  return {
    ...empty,
    ...data,
    categoryBreakdown: Array.isArray(data.categoryBreakdown) && data.categoryBreakdown.length > 0
      ? data.categoryBreakdown
      : empty.categoryBreakdown,
    sixMonthTrend: Array.isArray(data.sixMonthTrend) && data.sixMonthTrend.length > 0
      ? data.sixMonthTrend
      : empty.sixMonthTrend
  };
}

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
  const chartData = normalizeInsights(insights, submittedPeriod);
  const queryError = query.error ? mapAxiosError(query.error) : null;
  const noTransactions = Boolean(submittedPeriod)
    && !query.isFetching
    && !queryError
    && (chartData.transactionCount ?? 0) === 0;

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">GET /accounts/{'{accountId}'}/insights</p>
          <h2>Spending Insights</h2>
          <p className="muted">Review debit-spend totals for a selected month as a pie chart plus a six-month bar chart trend.</p>
        </div>
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
        {noTransactions ? <div className="banner info">No eligible spending transactions were found for this month. Showing a zero-value category breakdown and trend.</div> : null}
      </section>

      <section className="panel stack">
        <div className="card-grid">
          <article className="metric">
            <p className="muted">Selected Period</p>
            <strong>{submittedPeriod || 'None selected'}</strong>
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