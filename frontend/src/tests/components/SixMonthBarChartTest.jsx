import { render, screen } from '@testing-library/react';
import SixMonthBarChart from '../../components/insights/SixMonthBarChart';

const makeTrend = () => [
  { year: 2025, month: 8, totalDebitSpend: 500, isComplete: true },
  { year: 2025, month: 9, totalDebitSpend: 0, isComplete: true },
  { year: 2025, month: 10, totalDebitSpend: 300, isComplete: true },
  { year: 2025, month: 11, totalDebitSpend: 200, isComplete: true },
  { year: 2025, month: 12, totalDebitSpend: 400, isComplete: true },
  { year: 2026, month: 1, totalDebitSpend: 150, isComplete: false },
];

describe('SixMonthBarChart', () => {
  it('always renders exactly 6 bar columns', () => {
    render(<SixMonthBarChart sixMonthTrend={makeTrend()} />);
    expect(screen.getAllByClass ? screen.getAllByClass('bar-column') : document.querySelectorAll('.bar-column')).toHaveLength(6);
  });

  it('zero-spend month renders a bar with 0px height', () => {
    render(<SixMonthBarChart sixMonthTrend={makeTrend()} />);
    const bars = document.querySelectorAll('.bar');
    // The second bar (month 9) has 0 spend
    expect(bars[1].style.height).toBe('0px');
  });

  it('in-progress month has visual indicator', () => {
    render(<SixMonthBarChart sixMonthTrend={makeTrend()} />);
    expect(screen.getByLabelText('In progress')).toBeInTheDocument();
  });
});
