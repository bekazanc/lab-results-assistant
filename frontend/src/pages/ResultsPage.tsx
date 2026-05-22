import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getResults } from '../services/api';
import { LabResult } from '../types';

const ResultsPage: React.FC = () => {
  const [results, setResults] = useState<LabResult[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchResults();
  }, []);

  const fetchResults = async () => {
    try {
      const data = await getResults();
      setResults(data);
    } catch {
      navigate('/');
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    navigate('/');
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'CRITICAL': return '#e53e3e';
      case 'ABNORMAL': return '#dd6b20';
      case 'NORMAL': return '#38a169';
      default: return '#718096';
    }
  };

  if (loading) return <div style={styles.loading}>Yükleniyor...</div>;

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1 style={styles.title}>🏥 Lab Sonuçları</h1>
        <button style={styles.logoutBtn} onClick={logout}>Çıkış</button>
      </div>
      <div style={styles.grid}>
        {results.map(result => (
          <div key={result.id} style={styles.card} onClick={() => navigate(`/results/${result.id}`)}>
            <div style={styles.cardHeader}>
              <span style={styles.patientId}>{result.patientId}</span>
              <span style={{ ...styles.badge, background: getStatusColor(result.status) }}>
                {result.status}
              </span>
            </div>
            <p style={styles.detail}>📟 {result.deviceId}</p>
            <p style={styles.detail}>🕐 {new Date(result.timestamp).toLocaleString('tr-TR')}</p>
            <p style={styles.detail}>
              ⚠️ Anormal: {result.tests?.filter(t => t.isAbnormal).length ?? 0} / {result.tests?.length ?? 0} test
            </p>
          </div>
        ))}
      </div>
    </div>
  );
};

const styles: Record<string, React.CSSProperties> = {
  container: { minHeight: '100vh', background: '#f0f4f8', padding: '24px' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' },
  title: { color: '#1a365d', margin: 0 },
  logoutBtn: { padding: '8px 16px', background: '#e53e3e', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '16px' },
  card: { background: 'white', padding: '20px', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', cursor: 'pointer', transition: 'transform 0.2s' },
  cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' },
  patientId: { fontWeight: 'bold', fontSize: '16px', color: '#2d3748' },
  badge: { padding: '4px 10px', borderRadius: '20px', color: 'white', fontSize: '12px', fontWeight: 'bold' },
  detail: { color: '#718096', fontSize: '14px', margin: '4px 0' },
  loading: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '18px' },
};

export default ResultsPage;
