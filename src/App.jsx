import { useEffect, useRef, useState } from 'react';
import { NavLink, Navigate, Outlet, Route, Routes, useNavigate } from 'react-router-dom';
import { useAuth } from './auth/AuthContext';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { AccountDetailPage } from './pages/AccountDetailPage';
import { AccountListPage } from './pages/AccountListPage';
import { CustomerCreatePage } from './pages/CustomerCreatePage';
import { CustomerDetailPage } from './pages/CustomerDetailPage';
import { CustomerEditPage } from './pages/CustomerEditPage';
import { CustomerProfilePage } from './pages/CustomerProfilePage';

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

function ProfileDropdown({ onClose }) {
  const navigate = useNavigate();
  const { logout } = useAuth();

  function handleProfile() {
    onClose();
    navigate('/customer-profile');
  }

  function handleLogout() {
    onClose();
    logout();
  }

  return (
    <div className="navbar-dropdown-menu">
      <button type="button" className="navbar-dropdown-item" onClick={handleProfile}>
        Profile
      </button>
      <button type="button" className="navbar-dropdown-item danger" onClick={handleLogout}>
        Log Out
      </button>
    </div>
  );
}

function AppLayout() {
  const { authState, isAdmin, isAuthenticated } = useAuth();
  const customerId = authState.customerId;
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    if (!profileMenuOpen) return;
    function handleOutsideClick(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setProfileMenuOpen(false);
      }
    }
    document.addEventListener('mousedown', handleOutsideClick);
    return () => document.removeEventListener('mousedown', handleOutsideClick);
  }, [profileMenuOpen]);

  return (
    <div className="app-shell">
      <header className="navbar">
        <span className="navbar-brand">FDM</span>
        <div className="navbar-actions">
          {isAuthenticated ? (
            <div className="navbar-profile" ref={dropdownRef}>
              <button
                type="button"
                className="navbar-avatar-btn"
                aria-label="Open profile menu"
                aria-expanded={profileMenuOpen}
                onClick={() => setProfileMenuOpen((open) => !open)}
              >
                <span className="navbar-avatar-initials">
                  {authState.username ? authState.username[0].toUpperCase() : 'U'}
                </span>
              </button>
              {profileMenuOpen && <ProfileDropdown onClose={() => setProfileMenuOpen(false)} />}
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
            {isAdmin && <NavLink to="/customers">Customers</NavLink>}
            {customerId && <NavLink to={`/customer/${customerId}/accounts`}>Customer Accounts</NavLink>}
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
          <Route path="/customer-profile" element={<CustomerProfilePage />} />
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
