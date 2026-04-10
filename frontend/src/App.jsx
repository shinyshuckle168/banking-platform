import { useState } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import TransactionHistoryPage from "./components/transactions/TransactionHistoryPage";
import StandingOrdersPage from "./components/standingorders/StandingOrdersPage";
import MonthlyStatementPage from "./components/statements/MonthlyStatementPage";
import SpendingInsightsPage from "./components/insights/SpendingInsightsPage";

const queryClient = new QueryClient();

const TABS = [
  { id: "transactions", label: "Transaction History" },
  { id: "standing-orders", label: "Standing Orders" },
  { id: "statements", label: "Monthly Statement" },
  { id: "insights", label: "Spending Insights" },
];

const DEMO_ACCOUNT_ID = 1;

function App() {
  const [activeTab, setActiveTab] = useState("transactions");

  return (
    <QueryClientProvider client={queryClient}>
      <div className="app-shell">
        <header className="app-topbar">
          <div className="app-topbar-logo">Digital <span>Banking</span></div>
          <span style={{ fontSize: 12, color: "#94a3b8" }}>Demo · Account #{DEMO_ACCOUNT_ID}</span>
        </header>

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
          {activeTab === "transactions" && <TransactionHistoryPage accountId={DEMO_ACCOUNT_ID} />}
          {activeTab === "standing-orders" && <StandingOrdersPage accountId={DEMO_ACCOUNT_ID} />}
          {activeTab === "statements" && <MonthlyStatementPage accountId={DEMO_ACCOUNT_ID} />}
          {activeTab === "insights" && <SpendingInsightsPage accountId={DEMO_ACCOUNT_ID} />}
        </main>
      </div>
    </QueryClientProvider>
  );
}

export default App;
