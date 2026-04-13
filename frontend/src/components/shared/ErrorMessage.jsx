/**
 * Renders a standardised error message. (T018)
 * @param {string} code    - Error code from server
 * @param {string} message - Human-readable error message
 * @param {string} [field] - Field name when error is field-level
 */
const ErrorMessage = ({ code, message, field }) => (
  <div role="alert" className="error-msg">
    <span>&#9888;</span>
    <span>
      {field && <strong>{field}: </strong>}
      {message || 'An error occurred'}
      {code && <span style={{ opacity: 0.7 }}> [{code}]</span>}
    </span>
  </div>
);

export default ErrorMessage;
