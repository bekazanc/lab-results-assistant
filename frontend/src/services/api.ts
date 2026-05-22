import axios from 'axios';

const BASE_URL = 'http://localhost:8080';

const api = axios.create({ baseURL: BASE_URL });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const login = async (username: string, password: string) => {
  const res = await axios.post(`${BASE_URL}/api/auth/login`, { username, password });
  return res.data.token;
};

export const getResults = async () => {
  const res = await api.get('/api/results');
  return res.data;
};

export const getResultById = async (id: number) => {
  const res = await api.get(`/api/results/${id}`);
  return res.data;
};

export const analyzeResult = async (id: number) => {
  const res = await api.post(`/api/results/${id}/analyze`);
  return res.data;
};

export default api;
