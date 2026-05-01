import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '60vh',
      textAlign: 'center',
      gap: '1rem',
    }}>
      <h2 style={{
        fontSize: '2rem',
        fontWeight: 700,
        color: 'var(--ink)',
        margin: 0,
      }}>
        Page not found
      </h2>
      <p className="muted" style={{ fontSize: '1rem', maxWidth: '360px', margin: 0 }}>
        We're sorry, the page you are looking for does not exist or has been moved.
      </p>
      <Link
        to="/"
        style={{
          marginTop: '0.5rem',
          display: 'inline-block',
          background: '#00684a',
          color: '#fff',
          fontWeight: 600,
          fontSize: '1rem',
          padding: '0.75rem 2rem',
          borderRadius: '0.5rem',
          textDecoration: 'none',
          letterSpacing: '0.01em',
        }}
      >
        Back to Overview
      </Link>
    </div>
  );
}
