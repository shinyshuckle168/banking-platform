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
      <aside className="sidebar">
        <div className="stack tight-gap">
          <p className="eyebrow">Banking Platform</p>
          <h1>Digital Banking Frontend</h1>
          <p className="lede">
            React client for the merged banking backend, including authentication, customer management, accounts, deposits, and withdrawals.
          </p>
        </div>
        <section className="panel stack tight-gap auth-summary">
          <p className="eyebrow">Session</p>
          {isAuthenticated ? (
            <>
              <strong>{authState.username || 'Authenticated user'}</strong>
              <p className="muted compact-text">
                Roles: {authState.roles.length > 0 ? authState.roles.join(', ') : 'None'}
              </p>
              <p className="muted compact-text">
                Customer context: {customerId || 'Not linked yet'}
              </p>
              <button type="button" className="secondary" onClick={logout}>Log Out</button>
            </>
          ) : (
            <p className="muted compact-text">Sign in or register to access customer and account flows.</p>
          )}
        </section>
        <nav className="nav-list">
          <NavLink to="/" end>Overview</NavLink>
          {!isAuthenticated ? <NavLink to="/login">Login</NavLink> : null}
          {!isAuthenticated ? <NavLink to="/register">Register</NavLink> : null}
          {!isAuthenticated ? <NavLink to="/password-reset">Password Reset</NavLink> : null}
          {isAuthenticated && !customerId ? <NavLink to="/customer/create">Create Customer</NavLink> : null}
          {isAuthenticated && (isAdmin || customerId) ? <NavLink to={isAdmin ? '/customer' : `/customer/${customerId}`} end>Customer Profile</NavLink> : null}
          {isAuthenticated && customerId ? <NavLink to={`/customer/${customerId}/accounts`}>Customer Accounts</NavLink> : null}
          {isAuthenticated && isAdmin ? <p className="nav-hint">Admin delete actions are available inside customer and account details.</p> : null}
        </nav>
      </aside>
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
