import { useState } from 'react';
import { useCreateStandingOrder } from '../../hooks/useStandingOrders';
import ErrorMessage from '../shared/ErrorMessage';

const FREQUENCIES = ['DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY'];

/**
 * Controlled form for creating a standing order. (T063)
 * Displays field-level errors from server 400/422 responses.
 */
const CreateStandingOrderForm = ({ accountId }) => {
  const [form, setForm] = useState({
    payeeAccount: '',
    payeeName: '',
    amount: '',
    frequency: '',
    startDate: '',
    endDate: '',
    reference: '',
  });
  const [fieldErrors, setFieldErrors] = useState({});

  const createMutation = useCreateStandingOrder(accountId);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    setFieldErrors((prev) => ({ ...prev, [name]: undefined }));
  };

  const isValid =
    form.payeeAccount.trim() &&
    form.payeeName.trim() &&
    form.amount &&
    form.frequency &&
    form.startDate;

  const handleSubmit = (e) => {
    e.preventDefault();
    setFieldErrors({});
    const payload = {
      payeeAccount: form.payeeAccount,
      payeeName: form.payeeName,
      amount: parseFloat(form.amount),
      frequency: form.frequency,
      startDate: form.startDate,
      endDate: form.endDate || undefined,
      reference: form.reference || undefined,
    };
    createMutation.mutate(payload, {
      onError: (error) => {
        const serverError = error?.response?.data;
        if (serverError?.field) {
          setFieldErrors({ [serverError.field]: serverError.message });
        }
      },
    });
  };

  return (
    <form onSubmit={handleSubmit} className="create-standing-order-form">
      <div className="form-grid">
        <div className="form-field">
          <label htmlFor="so-payeeAccount">Payee Account *</label>
          <input id="so-payeeAccount" name="payeeAccount" value={form.payeeAccount} onChange={handleChange} required />
          {fieldErrors.payeeAccount && <span className="field-error">{fieldErrors.payeeAccount}</span>}
        </div>

        <div className="form-field">
          <label htmlFor="so-payeeName">Payee Name *</label>
          <input id="so-payeeName" name="payeeName" value={form.payeeName} onChange={handleChange} required />
          {fieldErrors.payeeName && <span className="field-error">{fieldErrors.payeeName}</span>}
        </div>

        <div className="form-field">
          <label htmlFor="so-amount">Amount (CAD) *</label>
          <input id="so-amount" name="amount" type="number" min="0.01" step="0.01" value={form.amount} onChange={handleChange} required />
          {fieldErrors.amount && <span className="field-error">{fieldErrors.amount}</span>}
        </div>

        <div className="form-field">
          <label htmlFor="so-frequency">Frequency *</label>
          <select id="so-frequency" name="frequency" value={form.frequency} onChange={handleChange} required>
            <option value="">Select frequency</option>
            {FREQUENCIES.map((f) => <option key={f} value={f}>{f}</option>)}
          </select>
          {fieldErrors.frequency && <span className="field-error">{fieldErrors.frequency}</span>}
        </div>

        <div className="form-field">
          <label htmlFor="so-startDate">Start Date *</label>
          <input id="so-startDate" name="startDate" type="datetime-local" value={form.startDate} onChange={handleChange} required />
          {fieldErrors.startDate && <span className="field-error">{fieldErrors.startDate}</span>}
        </div>

        <div className="form-field">
          <label htmlFor="so-endDate">End Date</label>
          <input id="so-endDate" name="endDate" type="datetime-local" value={form.endDate} onChange={handleChange} />
        </div>

        <div className="form-field">
          <label htmlFor="so-reference">Reference</label>
          <input id="so-reference" name="reference" value={form.reference} onChange={handleChange} maxLength={140} />
        </div>
      </div>

      {createMutation.isError && !Object.keys(fieldErrors).length && (() => {
        const err = createMutation.error?.response?.data;
        return <ErrorMessage code={err?.code} message={err?.message || 'Failed to create standing order.'} />;
      })()}

      <button type="submit" className="btn btn-primary" disabled={!isValid || createMutation.isPending}>
        {createMutation.isPending ? 'Creating…' : 'Create Standing Order'}
      </button>
    </form>
  );
};

export default CreateStandingOrderForm;
