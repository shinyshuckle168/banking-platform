import ErrorMessage from '../shared/ErrorMessage';
import { useTransactionExport } from '../../hooks/useTransactionExport';

/**
 * Export PDF button with loading state and error feedback. (T038)
 */
const ExportButton = ({ accountId, startDate, endDate }) => {
  const { mutate, isPending, isError, error } = useTransactionExport(accountId, startDate, endDate);

  const serverError = error?.response?.data;

  return (
    <div className="export-button-wrapper">
      <button onClick={() => mutate()} disabled={isPending} className="export-btn">
        {isPending ? 'Exporting...' : 'Export PDF'}
      </button>
      {isError && (
        <ErrorMessage
          code={serverError?.code}
          message={serverError?.message || 'Export failed. Please try again.'}
          field={serverError?.field}
        />
      )}
    </div>
  );
};

export default ExportButton;
