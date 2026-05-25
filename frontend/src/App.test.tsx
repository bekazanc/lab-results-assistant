import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import App from './App';

test('renders login page by default', () => {
  render(<App />);
  expect(screen.getByText('Doktor Girişi')).toBeInTheDocument();
});
