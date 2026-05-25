import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import * as api from '../services/api';

jest.mock('../services/api');

describe('LoginPage', () => {
  test('renders login form', () => {
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );
    expect(screen.getByPlaceholderText('Kullanıcı adı')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Şifre')).toBeInTheDocument();
    expect(screen.getByText('Giriş Yap')).toBeInTheDocument();
  });

  test('shows error on failed login', async () => {
    (api.login as jest.Mock).mockRejectedValue(new Error('Unauthorized'));

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText('Kullanıcı adı'), {
      target: { value: 'wronguser' },
    });
    fireEvent.change(screen.getByPlaceholderText('Şifre'), {
      target: { value: 'wrongpass' },
    });
    fireEvent.click(screen.getByText('Giriş Yap'));

    await waitFor(() => {
      expect(screen.getByText('Geçersiz kullanıcı adı veya şifre')).toBeInTheDocument();
    });
  });

  test('redirects on successful login', async () => {
    (api.login as jest.Mock).mockResolvedValue('mock-token');

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText('Kullanıcı adı'), {
      target: { value: 'doctor' },
    });
    fireEvent.change(screen.getByPlaceholderText('Şifre'), {
      target: { value: 'doctor123' },
    });
    fireEvent.click(screen.getByText('Giriş Yap'));

    await waitFor(() => {
      expect(localStorage.getItem('token')).toBe('mock-token');
    });
  });
});
