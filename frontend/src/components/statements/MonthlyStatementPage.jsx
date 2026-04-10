import { useState } from 'react';
import LoadingSpinner from '../shared/LoadingSpinner';
import ErrorMessage from '../shared/ErrorMessage';
import StatementViewer from './StatementViewer';
import { useMonthlyStatement } from '../../hooks/useMonthlyStatement';

/**
 * Monthly Statement page with period and optional version inputs. (T089)
 */
const MonthlyStatementPage = ({ accountId }) => {
  const [period, setPeriod] = useState('');
  const [version, setVersion] = useState('');

  const { data, isLoading, isError, error } = useMonthlyStatement(
    accountId,
    period || undefined,
    version ? parseInt(version, 10) : undefined
  );

  const serverError = error?.response?.data;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Monthly Statement</h1>
      </div>

      <div className="filter-row">
        <div className="filter-group">
          <label>Period (YYYY-MM)</label>
          <input
            type="text"
            placeholder="2026-03"
            value={period}
            onChange={(e) => setPeriod(e.target.value)}
            pattern="\d{4}-\d{2}"
            style={{ minWidth: 130 }}
          />
        </div>
        <div className="filter-group">
          <label>Version (optional)</label>
          <input
            type="number"
            min="1"
            value={version}
            onChange={(e) => setVersion(e.target.value)}
            style={{ minWidth: 80 }}
          />
        </div>
      </div>

      {isLoading && <LoadingSpinner />}

      {isError && (
        <ErrorMessage
          code={serverError?.code}
          message={serverError?.message || 'Failed to load statement.'}
          field={serverError?.field}
        />
      )}

      {data && <StatementViewer statement={data} />}
    </div>
  );
};

export default MonthlyStatementPage;
