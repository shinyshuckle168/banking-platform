import { useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { mapAxiosError } from '../api/axiosClient';
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

function parseYearMonth(period) {
  const match = String(period || '').match(/^(\d{4})-(\d{2})$/);
  if (!match) {
    return null;
  }

  return {
    year: Number(match[1]),
    month: Number(match[2])
  };
}

function isFuturePeriod(period) {
  const parsed = parseYearMonth(period);
  if (!parsed) {
    return false;
  }

  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth() + 1;
  return parsed.year > currentYear || (parsed.year === currentYear && parsed.month > currentMonth);
}

function resolveStatementErrorMessage(queryError, submittedPeriod) {
  if (!queryError) {
    return null;
  }

  if (isFuturePeriod(submittedPeriod)) {
    return 'Statement month cannot be in the future. Please choose the current month or an earlier month.';
  }

  if (queryError.code === 'HTTP_500' || queryError.code === 'INTERNAL_SERVER_ERROR') {
    return 'Statement is not available for the selected month. Please choose a month when this account was already open.';
  }

  return queryError.message;
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

  const statementPdf = query.data;
  const queryError = query.error ? mapAxiosError(query.error) : null;
  const errorMessage = resolveStatementErrorMessage(queryError, submittedPeriod);

  function handleDownloadStatement() {
    if (!statementPdf || !submittedPeriod) {
      return;
    }

    const downloadUrl = window.URL.createObjectURL(statementPdf);
    const anchor = document.createElement('a');
    anchor.href = downloadUrl;
    anchor.download = `statement-${accountId}-${submittedPeriod}.pdf`;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    window.URL.revokeObjectURL(downloadUrl);
  }

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">GET /accounts/{'{accountId}'}/statements/{'{period}'}</p>
          <h2>Monthly Statement</h2>
          <p className="muted">Request a monthly statement PDF for a specific year and month.</p>
        </div>
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
        {errorMessage ? <div className="banner error">{errorMessage}</div> : null}
        {!submittedPeriod ? <div className="banner info">Pick a statement month and click Load Statement to request data.</div> : null}
      </section>

      <section className="panel stack">
        <div className="section-header">
          <div>
            <h3>Statement File</h3>
            <p className="muted">Requested period: {submittedPeriod || 'None selected'}</p>
          </div>
        </div>
        {statementPdf ? (
          <div className="actions">
            <button type="button" onClick={handleDownloadStatement}>Download Statement PDF</button>
          </div>
        ) : (
          <div className="banner info">Load a statement period to generate a downloadable PDF.</div>
        )}
      </section>
    </div>
  );
}