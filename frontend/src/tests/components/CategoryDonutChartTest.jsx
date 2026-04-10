import { render, screen } from '@testing-library/react';
import CategoryDonutChart from '../../components/insights/CategoryDonutChart';

const makeBreakdown = () => [
  { category: 'Housing', percentage: 30 },
  { category: 'Transport', percentage: 15 },
  { category: 'Food & Drink', percentage: 20 },
  { category: 'Entertainment', percentage: 5 },
  { category: 'Shopping', percentage: 10 },
  { category: 'Utilities', percentage: 8 },
  { category: 'Health', percentage: 7 },
  { category: 'Income', percentage: 5 },
];

describe('CategoryDonutChart', () => {
  it('always renders all 8 segments (legend items)', () => {
    render(<CategoryDonutChart categoryBreakdown={makeBreakdown()} />);
    const items = screen.getAllByRole('listitem');
    expect(items).toHaveLength(8);
  });

  it('zero-value category label shows 0% and does not error', () => {
    const breakdown = makeBreakdown().map((c, i) => (i === 0 ? { ...c, percentage: 0 } : c));
    render(<CategoryDonutChart categoryBreakdown={breakdown} />);
    expect(screen.getByText('0.0%')).toBeInTheDocument();
  });

  it('renders all 8 category names', () => {
    render(<CategoryDonutChart categoryBreakdown={makeBreakdown()} />);
    ['Housing', 'Transport', 'Food & Drink', 'Entertainment', 'Shopping', 'Utilities', 'Health', 'Income']
      .forEach((cat) => expect(screen.getByText(cat)).toBeInTheDocument());
  });
});
