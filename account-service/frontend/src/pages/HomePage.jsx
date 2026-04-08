export function HomePage() {
  return (
    <div className="stack">
      <section className="hero">
        <span className="kicker">Current delivery slice</span>
        <h2>Frontend for the implemented account flows</h2>
        <p className="lede">
          This UI is wired to the backend endpoints that already exist: account CRUD, customer delete, and the new deposit, withdraw, and transfer flows.
        </p>
        <div className="hero-grid">
          <article className="metric">
            <p className="muted">Available endpoints</p>
            <strong>9</strong>
          </article>
          <article className="metric">
            <p className="muted">Frontend stack</p>
            <strong>React + Query</strong>
          </article>
          <article className="metric">
            <p className="muted">Auth mode</p>
            <strong>Scaffold headers</strong>
          </article>
        </div>
      </section>
      <section className="card-grid">
        <article className="panel">
          <h3>Create Account</h3>
          <p className="muted">Type-aware form for CHECKING and SAVINGS account creation.</p>
        </article>
        <article className="panel">
          <h3>Account Detail</h3>
          <p className="muted">Lookup a specific account and inspect the mapped response payload.</p>
        </article>
        <article className="panel">
          <h3>Customer Accounts</h3>
          <p className="muted">List active accounts for a customer and handle empty states cleanly.</p>
        </article>
        <article className="panel">
          <h3>Money Movement</h3>
          <p className="muted">Submit deposits, withdrawals, and transfers with explicit idempotency keys.</p>
        </article>
      </section>
    </div>
  );
}
