import { Link, useNavigate } from 'react-router-dom';
import { useEffect, useRef, useState } from 'react';
import { getCustomer } from '../api/customers';
import { mapAxiosError } from '../api/axiosClient';
import { useAuth } from '../auth/AuthContext';
import { emptyCustomerLookup } from '../types';
import offerImage1 from '../images/image_1.png';
import offerImage2 from '../images/image_2.jpg';
import offerImage3 from '../images/image_3.jpg';

const offers = [
  {
    title: 'Zero-Fee Starter Account',
    description: 'Open your first account with no monthly maintenance fee for the first 12 months.',
    badge: 'New',
    image: offerImage1
  },
  {
    title: 'Smart Savings Boost',
    description: 'Earn up to 4.10% variable APY with automated monthly round-up deposits.',
    badge: 'Popular',
    image: offerImage2
  },
  {
    title: 'Travel Rewards Card',
    description: 'Get 2x points on flights and hotels, plus no foreign transaction fees.',
    badge: 'Limited Offer',
    image: offerImage3
  }
];

export function HomePage() {
  const navigate = useNavigate();
  const { authState, isAuthenticated, isAdmin, rememberCustomerId } = useAuth();
  const [lookup, setLookup] = useState(emptyCustomerLookup);
  const [activeOfferIndex, setActiveOfferIndex] = useState(0);
  // ...existing code...
  const intervalRef = useRef(null);

  function startSliderTimer() {
    window.clearInterval(intervalRef.current);
    intervalRef.current = window.setInterval(() => {
      setActiveOfferIndex((current) => (current + 1) % offers.length);
    }, 4500);
  }

  useEffect(() => {
    startSliderTimer();
    return () => window.clearInterval(intervalRef.current);
  }, []);

  function openCustomer() {
    if (lookup.customerId) {
      navigate(`/customer/${lookup.customerId}`);
    }
  }

  function openAccount() {
    if (lookup.accountId) {
      navigate(`/accounts/${lookup.accountId}`);
    }
  }

  // ...existing code...

  return (
    <div className="stack">
      <section className="stack offers-panel">
        <div className="offers-header">
          <div>
            <span className="kicker">Featured offers</span>
            <h3>Slide through this month&apos;s banking offers</h3>
          </div>
        </div>
        <div className="offers-slider-window">
          <div
            className="offers-slider-track"
            style={{ transform: `translateX(-${activeOfferIndex * 100}%)` }}
          >
            {offers.map((offer) => (
              <article key={offer.title} className="offer-slide" role="tabpanel">
                <div className="offer-copy">
                  <p className="offer-badge">{offer.badge}</p>
                  <h4>{offer.title}</h4>
                  <p className="muted">{offer.description}</p>
                </div>
                <div className="offer-media">
                  <img src={offer.image} alt={offer.title} />
                </div>
              </article>
            ))}
          </div>
        </div>
        <div className="offers-controls bottom" role="tablist" aria-label="Offer slides">
          {offers.map((offer, index) => (
            <button
              key={offer.title}
              type="button"
              className={index === activeOfferIndex ? 'offer-dot active' : 'offer-dot'}
              onClick={() => { setActiveOfferIndex(index); startSliderTimer(); }}
              aria-label={`Show offer ${index + 1}`}
              aria-selected={index === activeOfferIndex}
              role="tab"
            />
          ))}
        </div>
      </section>

      {!isAuthenticated ? (
        <Link className="big-get-started" to="/login">Get Started</Link>
      ) : (
        <section className="panel stack">
          <h3>Quick Navigation</h3>
          {isAdmin ? (
            <>
              <div className="form-grid">
                <div className="field">
                  <label htmlFor="home-customer-id">Open Customer ID</label>
                  <input
                    id="home-customer-id"
                    value={lookup.customerId}
                    onChange={(event) => setLookup((current) => ({ ...current, customerId: event.target.value }))}
                    placeholder={authState.customerId || 'Enter customer ID'}
                  />
                </div>
                <div className="field">
                  <label htmlFor="home-account-id">Open Account ID</label>
                  <input
                    id="home-account-id"
                    value={lookup.accountId}
                    onChange={(event) => setLookup((current) => ({ ...current, accountId: event.target.value }))}
                    placeholder="Enter account ID"
                  />
                </div>
              </div>
            </>
          ) : (
            <>
              {/* Removed dummy Link My Profile UI */}
            </>
          )}
          <div className="actions">
            {isAdmin ? <button type="button" onClick={openCustomer} disabled={!lookup.customerId}>Open Customer</button> : null}
            {isAdmin ? <button type="button" onClick={openAccount} disabled={!lookup.accountId}>Open Account</button> : null}
            {authState.customerId ? <Link className="button-link" to={`/customer/${authState.customerId}`}>My Profile</Link> : null}
            {authState.customerId ? <Link className="button-link subtle" to={`/customer/${authState.customerId}/accounts`}>My Accounts</Link> : null}
            {/* Removed Link My Profile button */}
            {!authState.customerId ? <Link className="button-link subtle" to="/customer/create">Create Customer</Link> : null}
          </div>
        </section>
      )}

    </div>
  );
}
