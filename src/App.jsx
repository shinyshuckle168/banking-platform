import { NavLink, Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth/AuthContext';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { AccountDetailPage } from './pages/AccountDetailPage';
import { AccountListPage } from './pages/AccountListPage';
import { CustomerCreatePage } from './pages/CustomerCreatePage';
import { CustomerDetailPage } from './pages/CustomerDetailPage';
import { CustomerEditPage } from './pages/CustomerEditPage';

import { DepositPage } from './pages/DepositPage';
import { MonthlyStatementPage } from './pages/MonthlyStatementPage';
import { CreateAccountPage } from './pages/CreateAccountPage';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { PasswordResetPage } from './pages/PasswordResetPage';
import { RegisterPage } from './pages/RegisterPage';
import { SpendingInsightsPage } from './pages/SpendingInsightsPage';
import { StandingOrdersPage } from './pages/StandingOrdersPage';
import { TransactionHistoryPage } from './pages/TransactionHistoryPage';
import { TransferPage } from './pages/TransferPage';
import { WithdrawPage } from './pages/WithdrawPage';

function getDefaultAuthenticatedRoute(authState) {
  const isAdmin = authState.roles.includes('ADMIN') || authState.roles.includes('ROLE_ADMIN');

  if (isAdmin) {
    return '/customer';
  }

  if (authState.customerId) {
    return `/customer/${authState.customerId}`;
  }

  return '/';
}

function PublicOnlyRoute({ children }) {
  const { authState, isAuthenticated } = useAuth();

  if (isAuthenticated) {
    return <Navigate to={getDefaultAuthenticatedRoute(authState)} replace />;
  }

  return children;
}

function AppLayout() {
  const { authState, isAdmin, isAuthenticated, logout } = useAuth();
  const customerId = authState.customerId;

  return (
    <div className="app-shell">
      <header className="navbar">
        <span className="navbar-brand">FDM</span>
        <div className="navbar-actions">
          {isAuthenticated ? (
            <div className="navbar-profile">
              <img
                src="https://via.placeholder.com/32"
                alt="Profile"
                className="profile-avatar"
              />
              <button type="button" className="secondary" onClick={logout}>Log Out</button>
            </div>
          ) : (
            <>
              <NavLink className="button-link subtle" to="/login">Login</NavLink>
              <NavLink className="button-link" to="/register">Get Started</NavLink>
            </>
          )}
        </div>
      </header>
      {isAuthenticated && (
        <div className="subnav">
          <nav className="subnav-list">
            <NavLink to="/" end>Overview</NavLink>
            {(isAdmin || customerId) && (
              <NavLink to={isAdmin ? '/customer' : `/customer/${customerId}`} end>
                Customer Profile
              </NavLink>
            )}
            {customerId && (
              <NavLink to={`/customer/${customerId}/accounts`}>Customer Accounts</NavLink>
            )}
          </nav>
        </div>
      )}
      <main className="content-area">
        <Outlet />
      </main>
    </div>
  );
}

export default function App() {
  const { authState } = useAuth();
  const defaultAuthenticatedRoute = getDefaultAuthenticatedRoute(authState);

  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<PublicOnlyRoute><LoginPage /></PublicOnlyRoute>} />
        <Route path="/register" element={<PublicOnlyRoute><RegisterPage /></PublicOnlyRoute>} />
        <Route path="/password-reset" element={<PublicOnlyRoute><PasswordResetPage /></PublicOnlyRoute>} />

        <Route element={<ProtectedRoute />}>
          <Route path="/customer/create" element={<CustomerCreatePage />} />
          <Route path="/customer" element={<CustomerDetailPage />} />
          <Route path="/customer/:customerId" element={<CustomerDetailPage />} />
          <Route path="/customer/:customerId/edit" element={<CustomerEditPage />} />
          <Route path="/customer/:customerId/accounts" element={<AccountListPage />} />
          <Route path="/customer/:customerId/accounts/create" element={<CreateAccountPage />} />
          <Route path="/accounts/:accountId" element={<AccountDetailPage />} />
          <Route path="/accounts/:accountId/edit" element={<AccountDetailPage />} />
          <Route path="/accounts/:accountId/deposit" element={<DepositPage />} />
          <Route path="/accounts/:accountId/withdraw" element={<WithdrawPage />} />
          <Route path="/accounts/:accountId/transactions" element={<TransactionHistoryPage />} />
          <Route path="/accounts/:accountId/standing-orders" element={<StandingOrdersPage />} />
          <Route path="/accounts/:accountId/statements" element={<MonthlyStatementPage />} />
          <Route path="/accounts/:accountId/insights" element={<SpendingInsightsPage />} />
          <Route path="/accounts/transfer" element={<TransferPage />} />
        </Route>

        <Route path="*" element={<Navigate to={authState.accessToken ? defaultAuthenticatedRoute : '/'} replace />} />
      </Route>
    </Routes>
  );
}
