import { useState } from "react";
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import TransactionHistoryPage from "./components/transactions/TransactionHistoryPage";
import StandingOrdersPage from "./components/standingorders/StandingOrdersPage";
import MonthlyStatementPage from "./components/statements/MonthlyStatementPage";
import SpendingInsightsPage from "./components/insights/SpendingInsightsPage";
import LoginPage from "./components/auth/LoginPage";
import RegisterPage from "./components/auth/RegisterPage";
import ProtectedRoute from "./components/shared/ProtectedRoute";

const queryClient = new QueryClient();

const TABS = [
  { id: "transactions", label: "Transaction History" },
  { id: "standing-orders", label: "Standing Orders" },
  { id: "statements", label: "Monthly Statement" },
  { id: "insights", label: "Spending Insights" },
];

function decodeJwtPayload(token) {
  try {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
  } catch {
    return null;
  }
}

function Dashboard() {
  const [activeTab, setActiveTab] = useState("transactions");
  const [selectedAccountId, setSelectedAccountId] = useState('');
  const [accountInput, setAccountInput] = useState('');
  const navigate = useNavigate();

  const token = localStorage.getItem('jwt');
  const payload = token ? decodeJwtPayload(token) : null;
  const userId = payload?.sub ?? 'unknown';

  const handleAccountSelect = (e) => {
    e.preventDefault();
    const id = parseInt(accountInput, 10);
    if (!isNaN(id) && id > 0) {
      setSelectedAccountId(id);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('jwt');
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <header className="app-topbar">
        <div className="app-topbar-logo">Digital <span>Banking</span></div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <span style={{ fontSize: 12, color: "#94a3b8" }}>User: {userId}</span>
          {selectedAccountId && (
            <span style={{ fontSize: 12, color: "#94a3b8" }}>Account #{selectedAccountId}</span>
          )}
          <button
            onClick={handleLogout}
            style={{ fontSize: 12, padding: '4px 10px', cursor: 'pointer' }}
          >
            Sign out
          </button>
        </div>
      </header>

      {!selectedAccountId ? (
        <div style={{ maxWidth: 360, margin: '80px auto', padding: 24, textAlign: 'center' }}>
          <h2 style={{ marginBottom: 16 }}>Select your account</h2>
          <form onSubmit={handleAccountSelect} style={{ display: 'flex', gap: 8 }}>
            <input
              type="number"
              min="1"
              placeholder="Account ID"
              value={accountInput}
              onChange={(e) => setAccountInput(e.target.value)}
              required
              style={{ flex: 1, padding: '8px 12px', fontSize: 14 }}
            />
            <button type="submit" style={{ padding: '8px 16px' }}>Go</button>
          </form>
        </div>
      ) : (
        <>
          <nav className="app-nav">
            {TABS.map(tab => (
              <button
                key={tab.id}
                className={`nav-tab${activeTab === tab.id ? " active" : ""}`}
                onClick={() => setActiveTab(tab.id)}
              >
                {tab.label}
              </button>
            ))}
          </nav>

          <main className="app-content">
            {activeTab === "transactions" && <TransactionHistoryPage accountId={selectedAccountId} />}
            {activeTab === "standing-orders" && <StandingOrdersPage accountId={selectedAccountId} />}
            {activeTab === "statements" && <MonthlyStatementPage accountId={selectedAccountId} />}
            {activeTab === "insights" && <SpendingInsightsPage accountId={selectedAccountId} />}
          </main>
        </>
      )}
    </div>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route
            path="/*"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;

