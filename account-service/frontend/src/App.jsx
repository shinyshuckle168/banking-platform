import { NavLink, Route, Routes } from 'react-router-dom';
import { AuthPanel } from './components/AuthPanel';
import { AccountDetailPage } from './pages/AccountDetailPage';
import { AccountListPage } from './pages/AccountListPage';
import { DepositPage } from './pages/DepositPage';
import { CreateAccountPage } from './pages/CreateAccountPage';
import { CustomerPage } from './pages/CustomerPage';
import { HomePage } from './pages/HomePage';
import { TransferPage } from './pages/TransferPage';
import { WithdrawPage } from './pages/WithdrawPage';

export default function App() {
  const showAuthDebugPanel = import.meta.env.DEV;

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div>
          <p className="eyebrow">Banking Platform</p>
          <h1>Account Service Console</h1>
          <p className="lede">
            Demo client for the current backend slice: account management plus deposit, withdraw, and transfer flows.
          </p>
        </div>
        <nav className="nav-list">
          <NavLink to="/" end>Overview</NavLink>
          <NavLink to="/accounts/create">Create Account</NavLink>
          <NavLink to="/accounts/detail">Account Detail</NavLink>
          <NavLink to="/transactions/deposit">Deposit</NavLink>
          <NavLink to="/transactions/withdraw">Withdraw</NavLink>
          <NavLink to="/transactions/transfer">Transfer</NavLink>
          <NavLink to="/customers/accounts">Customer Accounts</NavLink>
          <NavLink to="/customers/manage">Customer Delete</NavLink>
        </nav>
        {showAuthDebugPanel ? <AuthPanel /> : null}
      </aside>
      <main className="content-area">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/accounts/create" element={<CreateAccountPage />} />
          <Route path="/accounts/detail" element={<AccountDetailPage />} />
          <Route path="/transactions/deposit" element={<DepositPage />} />
          <Route path="/transactions/withdraw" element={<WithdrawPage />} />
          <Route path="/transactions/transfer" element={<TransferPage />} />
          <Route path="/customers/accounts" element={<AccountListPage />} />
          <Route path="/customers/manage" element={<CustomerPage />} />
        </Routes>
      </main>
    </div>
  );
}
