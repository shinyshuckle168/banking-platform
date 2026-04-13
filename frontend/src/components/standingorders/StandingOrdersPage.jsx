import LoadingSpinner from '../shared/LoadingSpinner';
import ErrorMessage from '../shared/ErrorMessage';
import CreateStandingOrderForm from './CreateStandingOrderForm';
import StandingOrderList from './StandingOrderList';
import { useStandingOrders } from '../../hooks/useStandingOrders';

/**
 * Standing Orders page — create and list. (T064)
 */
const StandingOrdersPage = ({ accountId }) => {
  const { data, isLoading, isError, error } = useStandingOrders(accountId);
  const serverError = error?.response?.data;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Standing Orders</h1>
      </div>

      <div className="card">
        <p className="card-title">Create Standing Order</p>
        <CreateStandingOrderForm accountId={accountId} />
      </div>

      <div className="card">
        <p className="card-title">Active Orders</p>
        {isLoading && <LoadingSpinner />}
        {isError && (
          <ErrorMessage
            code={serverError?.code}
            message={serverError?.message || 'Failed to load standing orders.'}
          />
        )}
        {data && (
          <StandingOrderList orders={data.standingOrders || []} accountId={accountId} />
        )}
      </div>
    </div>
  );
};

export default StandingOrdersPage;
