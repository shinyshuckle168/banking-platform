/**
 * Loading spinner component. (T019)
 */
const LoadingSpinner = () => (
  <div role="status" aria-label="Loading" className="spinner-wrap">
    <div className="spinner" aria-hidden="true" />
    <span className="sr-only">Loading...</span>
  </div>
);

export default LoadingSpinner;
