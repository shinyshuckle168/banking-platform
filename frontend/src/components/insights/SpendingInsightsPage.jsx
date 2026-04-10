import { useState } from 'react';
import LoadingSpinner from '../shared/LoadingSpinner';
import ErrorMessage from '../shared/ErrorMessage';
import CategoryDonutChart from './CategoryDonutChart';
import SixMonthBarChart from './SixMonthBarChart';
import TransactionCategoryRow from './TransactionCategoryRow';
import { useSpendingInsights } from '../../hooks/useSpendingInsights';

/**
 * Spending Insights page with year/month picker and charts. (T108)
 */
const SpendingInsightsPage = ({ accountId }) => {
  const now = new Date();
  const [year, setYear] = useState(String(now.getFullYear()));
  const [month, setMonth] = useState(String(now.getMonth() + 1));

  const { data, isLoading, isError, error } = useSpendingInsights(
    accountId,
    parseInt(year, 10),
    parseInt(month, 10)
  );

  const serverError = error?.response?.data;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Spending Insights</h1>
      </div>

      <div className="filter-row">
        <div className="filter-group">
          <label>Year</label>
          <input type="number" min="2000" max="2100" value={year} onChange={(e) => setYear(e.target.value)} style={{ minWidth: 90 }} />
        </div>
        <div className="filter-group">
          <label>Month</label>
          <select value={month} onChange={(e) => setMonth(e.target.value)}>
            {['January','February','March','April','May','June','July','August','September','October','November','December'].map((name, i) => (
              <option key={i + 1} value={i + 1}>{name}</option>
            ))}
          </select>
        </div>
      </div>

      {isLoading && <LoadingSpinner />}

      {isError && (
        <ErrorMessage
          code={serverError?.code}
          message={serverError?.message || 'Failed to load spending insights.'}
          field={serverError?.field}
        />
      )}

      {data && (
        <>
          <div className="charts-stack">
            <div className="chart-card">
              <div className="chart-title">Spending by Category</div>
              <CategoryDonutChart categoryBreakdown={data.categoryBreakdown || []} />
            </div>
            <div className="chart-card">
              <div className="chart-title">6-Month Spending Trend</div>
              <SixMonthBarChart sixMonthTrend={data.sixMonthTrend || []} />
            </div>
          </div>

          {(data.topTransactions || []).length > 0 && (
            <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
              <div style={{ padding: '16px 20px 0' }}><p className="card-title">Top Transactions</p></div>
              <table className="txn-table">
                <thead>
                  <tr>
                    <th>Description</th>
                    <th>Amount</th>
                    <th>Category</th>
                  </tr>
                </thead>
                <tbody>
                  {data.topTransactions.map((tx) => (
                    <TransactionCategoryRow
                      key={tx.transactionId}
                      transaction={tx}
                      accountId={accountId}
                      year={parseInt(year, 10)}
                      month={parseInt(month, 10)}
                    />
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default SpendingInsightsPage;
