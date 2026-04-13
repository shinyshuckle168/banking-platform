import StandingOrderItem from './StandingOrderItem';

/**
 * Renders the list of standing orders, or an empty state. (T062)
 */
const StandingOrderList = ({ orders = [], accountId }) => {
  if (orders.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-state-icon">📄</div>
        <p>No standing orders found.</p>
      </div>
    );
  }

  return (
    <div className="so-grid">
      {orders.map((order) => (
        <StandingOrderItem key={order.standingOrderId} order={order} accountId={accountId} />
      ))}
    </div>
  );
};

export default StandingOrderList;
