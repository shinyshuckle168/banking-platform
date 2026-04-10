import { useState } from 'react';
import LoadingSpinner from '../shared/LoadingSpinner';
import ErrorMessage from '../shared/ErrorMessage';
import { useMonthlyStatement } from '../../hooks/useMonthlyStatement';

/**
 * Monthly Statement page. Generates and downloads a PDF bank statement
 * for the selected month and year. (T089)
 */
const MonthlyStatementPage = ({ accountId }) => {
  const currentDate = new Date();
  const [year, setYear] = useState(String(currentDate.getFullYear()));
  const [month, setMonth] = useState(String(currentDate.getMonth() + 1).padStart(2, '0'));

  const { mutate, isPending, isError, error, isSuccess } = useMonthlyStatement(accountId);

  const serverError = error?.response?.data;

  const handleDownload = () => {
    const period = `${year}-${month}`;
    mutate(period);
  };

  const years = [];
  for (let y = currentDate.getFullYear(); y >= currentDate.getFullYear() - 5; y--) {
    years.push(y);
  }

  const months = [
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
    { value: '12', label: 'December' },
  ];

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Monthly Statement</h1>
      </div>

      <div className="filter-row">
        <div className="filter-group">
          <label htmlFor="month-select">Month</label>
          <select id="month-select" value={month} onChange={(e) => setMonth(e.target.value)} style={{ minWidth: 130 }}>
            {months.map((m) => (
              <option key={m.value} value={m.value}>{m.label}</option>
            ))}
          </select>
        </div>
        <div className="filter-group">
          <label htmlFor="year-select">Year</label>
          <select id="year-select" value={year} onChange={(e) => setYear(e.target.value)} style={{ minWidth: 90 }}>
            {years.map((y) => (
              <option key={y} value={String(y)}>{y}</option>
            ))}
          </select>
        </div>
        <div className="filter-group" style={{ justifyContent: 'flex-end' }}>
          <button
            className="btn btn-primary"
            onClick={handleDownload}
            disabled={isPending || !accountId}
          >
            {isPending ? 'Generating…' : 'Download PDF'}
          </button>
        </div>
      </div>

      {isPending && <LoadingSpinner />}

      {isError && (
        <ErrorMessage
          code={serverError?.code}
          message={serverError?.message || 'Failed to generate statement.'}
          field={serverError?.field}
        />
      )}

      {isSuccess && !isPending && (
        <div style={{ marginTop: 16, color: 'var(--success-fg)' }}>
          Statement downloaded successfully.
        </div>
      )}
    </div>
  );
};

export default MonthlyStatementPage;

