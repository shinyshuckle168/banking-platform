import { useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { mapAxiosError } from '../api/axiosClient';
import { TransactionsTable } from '../components/TransactionsTable';
import { useMonthlyStatement } from '../hooks/useGroup3';
import { emptyMonthlyStatementLookup } from '../types';

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

export function MonthlyStatementPage() {
  const { accountId } = useParams();
  const [searchParams] = useSearchParams();
  const requestedPeriod = searchParams.get('period');
  const initialPeriod = requestedPeriod || emptyMonthlyStatementLookup.period;
  const initial = splitPeriod(initialPeriod);
  const [form, setForm] = useState({ year: initial.year, month: initial.month });
  const [submittedPeriod, setSubmittedPeriod] = useState('');
  const query = useMonthlyStatement({ accountId, period: submittedPeriod });

  function handleSubmit(event) {
    event.preventDefault();
    const period = joinPeriod(form.year, form.month);
    if (!period) {
      return;
    }

    setSubmittedPeriod(period);
  }

  const statement = query.data;
  const queryError = query.error ? mapAxiosError(query.error) : null;

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">GET /accounts/{'{accountId}'}/statements/{'{period}'}</p>
          <h2>Monthly Statement</h2>
          <p className="muted">Request the latest statement version for a specific year and month, then review it in a transaction-history-style layout.</p>
        </div>
        <div className="banner info">This page is scaffolded against the future Group 3 backend contract. Until that merge lands, live requests from this screen may fail.</div>
        <form className="form-grid" onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="statement-year">Statement Year</label>
            <input
              id="statement-year"
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
            <label htmlFor="statement-month">Statement Month</label>
            <select
              id="statement-month"
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
            <button type="submit" disabled={query.isFetching}>Load Statement</button>
            <Link className="button-link subtle" to={`/accounts/${accountId}`}>Back to Account</Link>
          </div>
        </form>
        {query.isLoading || query.isFetching ? <div className="banner success">Loading monthly statement...</div> : null}
        {queryError ? <div className="banner error">{queryError.message}</div> : null}
        {!submittedPeriod ? <div className="banner info">Pick a statement month and click Load Statement to request data.</div> : null}
      </section>

      <section className="panel stack">
        <div className="section-header">
          <div>
            <h3>Statement Summary</h3>
            <p className="muted">Requested period: {submittedPeriod || 'None selected'}</p>
          </div>
          <div className="detail-item">
            <p className="muted">Version</p>
            <strong>{statement?.versionNumber ?? 'Latest'}</strong>
          </div>
        </div>
        <div className="card-grid">
          <article className="metric">
            <p className="muted">Opening Balance</p>
            <strong>{statement?.openingBalance ?? '0.00'}</strong>
          </article>
          <article className="metric">
            <p className="muted">Closing Balance</p>
            <strong>{statement?.closingBalance ?? '0.00'}</strong>
          </article>
          <article className="metric">
            <p className="muted">Total Money In</p>
            <strong>{statement?.totalMoneyIn ?? '0.00'}</strong>
          </article>
          <article className="metric">
            <p className="muted">Total Money Out</p>
            <strong>{statement?.totalMoneyOut ?? '0.00'}</strong>
          </article>
        </div>
        {statement?.correctionSummary ? <div className="banner success">Correction Summary: {statement.correctionSummary}</div> : null}
        {statement?.generatedAt ? <p className="muted compact-text">Generated at: {new Date(statement.generatedAt).toLocaleString()}</p> : null}
        <TransactionsTable
          transactions={statement?.transactions}
          emptyTitle="No statement transactions returned"
          emptyMessage="The selected statement may have no activity, may not exist yet, or the future backend contract may still be unavailable."
        />
      </section>
    </div>
  );
}