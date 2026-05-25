import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getResults, searchByPatientId, getByStatus, getByDateRange } from '../services/api';
import { LabResult } from '../types';

const ResultsPage: React.FC = () => {
  const [results, setResults] = useState<LabResult[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [searching, setSearching] = useState(false);
  const [activeFilter, setActiveFilter] = useState<string>('ALL');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchResults();
  }, []);

  const fetchResults = async () => {
    try {
      const data = await getResults();
      const sorted = [...data].sort((a: LabResult, b: LabResult) => {
        const order: Record<string, number> = { CRITICAL: 0, ABNORMAL: 1, NORMAL: 2, INVALID: 3 };
        return order[a.status] - order[b.status];
      });
      setResults(sorted);
      setActiveFilter('ALL');
    } catch {
      navigate('/');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (value: string) => {
    setSearch(value);
    setActiveFilter('ALL');
    if (value.trim().length < 2) {
      fetchResults();
      return;
    }
    setSearching(true);
    try {
      const data = await searchByPatientId(value.trim());
      setResults(data);
    } catch {
      setResults([]);
    } finally {
      setSearching(false);
    }
  };

  const handleStatusFilter = async (status: string) => {
    setSearch('');
    setActiveFilter(status);
    if (status === 'ALL') {
      fetchResults();
      return;
    }
    try {
      const data = await getByStatus(status);
      setResults(data);
    } catch {
      setResults([]);
    }
  };

  const handleDateFilter = async () => {
    if (!startDate || !endDate) return;
    try {
      const start = new Date(startDate).toISOString().slice(0, 19);
      const end = new Date(endDate).toISOString().slice(0, 19);
      const data = await getByDateRange(start, end);
      setResults(data);
      setActiveFilter('DATE');
      setSearch('');
    } catch {
      setResults([]);
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

      <div style={styles.toolbar}>
        <input
          style={styles.searchBox}
          type="text"
          placeholder="🔍 Hasta ID ile arayınız..."
          value={search}
          onChange={e => handleSearch(e.target.value)}
        />
        <div style={styles.filterButtons}>
          {['ALL', 'CRITICAL', 'ABNORMAL', 'NORMAL'].map(status => (
            <button
              key={status}
              style={{
                ...styles.filterBtn,
                background: activeFilter === status ? getStatusColor(status) : 'white',
                color: activeFilter === status ? 'white' : getStatusColor(status),
                border: `2px solid ${getStatusColor(status)}`,
              }}
              onClick={() => handleStatusFilter(status)}
            >
              {status === 'ALL' ? 'Tümü' :
               status === 'CRITICAL' ? 'CRITICAL' :
               status === 'ABNORMAL' ? 'ABNORMAL' : 'NORMAL'}
            </button>
          ))}
        </div>
      </div>

      <div style={styles.dateFilter}>
        <input
          style={styles.dateInput}
          type="datetime-local"
          value={startDate}
          onChange={e => setStartDate(e.target.value)}
        />
        <span style={styles.dateSeparator}>—</span>
        <input
          style={styles.dateInput}
          type="datetime-local"
          value={endDate}
          onChange={e => setEndDate(e.target.value)}
        />
        <button
          style={{
            ...styles.filterBtn,
            background: activeFilter === 'DATE' ? '#3182ce' : 'white',
            color: activeFilter === 'DATE' ? 'white' : '#3182ce',
            border: '2px solid #3182ce',
          }}
          onClick={handleDateFilter}
        >
          📅 Tarihe Göre Filtrele
        </button>
        {activeFilter === 'DATE' && (
          <button style={styles.clearBtn} onClick={() => { fetchResults(); setStartDate(''); setEndDate(''); }}>
            ✕ Temizle
          </button>
        )}
      </div>

      {searching && <p style={styles.searchInfo}>Aranıyor...</p>}
      {!searching && search.trim().length >= 2 && (
        <p style={styles.searchInfo}>"{search}" için {results.length} sonuç bulundu.</p>
      )}
      {activeFilter === 'DATE' && (
        <p style={styles.searchInfo}>📅 Tarih aralığı için {results.length} sonuç bulundu.</p>
      )}

      {['CRITICAL', 'ABNORMAL', 'NORMAL'].map(group => {
        const grouped = results.filter(r => r.status === group);
        if (grouped.length === 0) return null;
        return (
          <div key={group} style={styles.section}>
            <h2 style={{ ...styles.groupTitle, color: getStatusColor(group) }}>
              {group === 'CRITICAL' ? '🚨' : group === 'ABNORMAL' ? '⚠️' : '✅'} {group} ({grouped.length})
            </h2>
            <div style={styles.grid}>
              {grouped.map(result => (
                <div
                  key={result.id}
                  style={styles.card}
                  onClick={() => navigate(`/results/${result.id}`)}
                  onMouseEnter={e => {
                    (e.currentTarget as HTMLDivElement).style.transform = 'scale(1.02)';
                    (e.currentTarget as HTMLDivElement).style.boxShadow = '0 8px 24px rgba(0,0,0,0.15)';
                  }}
                  onMouseLeave={e => {
                    (e.currentTarget as HTMLDivElement).style.transform = 'scale(1)';
                    (e.currentTarget as HTMLDivElement).style.boxShadow = '0 2px 8px rgba(0,0,0,0.08)';
                  }}
                >
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
      })}

      {results.length === 0 && !loading && !searching && (
        <p style={styles.noResult}>Sonuç bulunamadı.</p>
      )}
    </div>
  );
};

const styles: Record<string, React.CSSProperties> = {
  container: { minHeight: '100vh', background: '#f0f4f8', padding: '24px' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' },
  title: { color: '#1a365d', margin: 0 },
  logoutBtn: { padding: '8px 16px', background: '#e53e3e', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer' },
  toolbar: { display: 'flex', gap: '12px', alignItems: 'center', marginBottom: '12px', flexWrap: 'wrap' },
  searchBox: { flex: 1, minWidth: '200px', padding: '10px 16px', fontSize: '15px', border: '1px solid #e2e8f0', borderRadius: '10px', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' },
  filterButtons: { display: 'flex', gap: '8px', flexWrap: 'wrap' },
  filterBtn: { padding: '8px 14px', borderRadius: '20px', fontSize: '13px', fontWeight: 'bold', cursor: 'pointer', transition: 'all 0.2s' },
  dateFilter: { display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '24px', flexWrap: 'wrap' },
  dateInput: { padding: '8px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', fontSize: '14px' },
  dateSeparator: { color: '#718096', fontWeight: 'bold' },
  clearBtn: { padding: '8px 12px', background: '#718096', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontSize: '13px' },
  searchInfo: { color: '#718096', fontSize: '14px', marginBottom: '16px' },
  section: { marginBottom: '32px' },
  groupTitle: { margin: '0 0 12px 0', fontSize: '18px', fontWeight: 'bold' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '16px' },
  card: { background: 'white', padding: '20px', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', cursor: 'pointer', transition: 'transform 0.15s ease, box-shadow 0.15s ease' },
  cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' },
  patientId: { fontWeight: 'bold', fontSize: '16px', color: '#2d3748' },
  badge: { padding: '4px 10px', borderRadius: '20px', color: 'white', fontSize: '12px', fontWeight: 'bold' },
  detail: { color: '#718096', fontSize: '14px', margin: '4px 0' },
  loading: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '18px' },
  noResult: { textAlign: 'center', color: '#718096', fontSize: '16px', marginTop: '40px' },
};

export default ResultsPage;