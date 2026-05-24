import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getResultById, analyzeResult, reanalyzeResult } from '../services/api';
import { LabResult } from '../types';

const ResultDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [result, setResult] = useState<LabResult | null>(null);
  const [analysis, setAnalysis] = useState('');
  const [analyzing, setAnalyzing] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    getResultById(Number(id)).then(data => {
      setResult(data);
      if (data.llmAnalysis) {
        setAnalysis(data.llmAnalysis);
      }
    }).catch(() => navigate('/results'));
  }, [id, navigate]);

  const handleAnalyze = async () => {
    setAnalyzing(true);
    try {
      const text = await analyzeResult(Number(id));
      setAnalysis(text);
    } catch {
      setAnalysis('LLM servisi şu an kullanılamıyor.');
    } finally {
      setAnalyzing(false);
    }
  };

  const handleReanalyze = async () => {
    setAnalyzing(true);
    try {
      const text = await reanalyzeResult(Number(id));
      setAnalysis(text);
    } catch {
      setAnalysis('LLM servisi şu an kullanılamıyor.');
    } finally {
      setAnalyzing(false);
    }
  };

  if (!result) return <div style={styles.loading}>Yükleniyor...</div>;

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'CRITICAL': return '#e53e3e';
      case 'ABNORMAL': return '#dd6b20';
      case 'NORMAL': return '#38a169';
      default: return '#718096';
    }
  };

  return (
    <div style={styles.container}>
      <button style={styles.backBtn} onClick={() => navigate('/results')}>← Geri</button>
      <div style={styles.card}>
        <div style={styles.header}>
          <div>
            <h2 style={styles.patientId}>{result.patientId}</h2>
            <p style={styles.detail}>📟 {result.deviceId} | Tarih: {new Date(result.timestamp).toLocaleString('tr-TR')}</p>
          </div>
          <span style={{ ...styles.badge, background: getStatusColor(result.status) }}>
            {result.status}
          </span>
        </div>

        <h3 style={styles.sectionTitle}>Test Sonuçları</h3>
        <table style={styles.table}>
          <thead>
            <tr>
              <th style={styles.th}>Test</th>
              <th style={styles.th}>Değer</th>
              <th style={styles.th}>Birim</th>
              <th style={styles.th}>Normal Aralık</th>
              <th style={styles.th}>Durum</th>
            </tr>
          </thead>
          <tbody>
            {result.tests.map(test => (
              <tr key={test.id} style={{ background: test.isAbnormal ? '#fff5f5' : 'white' }}>
                <td style={styles.td}>{test.name}</td>
                <td style={{ ...styles.td, fontWeight: 'bold', color: test.isAbnormal ? '#e53e3e' : '#38a169' }}>
                  {test.value}
                </td>
                <td style={styles.td}>{test.unit}</td>
                <td style={styles.td}>{test.referenceMin} - {test.referenceMax}</td>
                <td style={styles.td}>{test.isAbnormal ? '⚠️ Anormal' : '✅ Normal'}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div style={styles.analysisSection}>
          {!analysis && (
            <button style={styles.analyzeBtn} onClick={handleAnalyze} disabled={analyzing}>
              {analyzing ? '🤖 Analiz yapılıyor...' : '🤖 Yapay Zeka Yorumu Al'}
            </button>
          )}

          {analysis && (
            <div style={styles.analysisBox}>
              <div style={styles.analysisHeader}>
                <h4 style={styles.analysisTitle}>🤖 Yapay Zeka Yorumu</h4>
                <button style={styles.reanalyzeBtn} onClick={handleReanalyze} disabled={analyzing}>
                  {analyzing ? 'Güncelleniyor...' : '🔄 Güncelle'}
                </button>
              </div>
              <p style={styles.analysisText}>{analysis}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

const styles: Record<string, React.CSSProperties> = {
  container: { minHeight: '100vh', background: '#f0f4f8', padding: '24px' },
  backBtn: { marginBottom: '16px', padding: '8px 16px', background: '#4a5568', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer' },
  card: { background: 'white', padding: '24px', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.08)' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px' },
  patientId: { color: '#1a365d', margin: '0 0 4px 0' },
  detail: { color: '#718096', fontSize: '14px', margin: 0 },
  badge: { padding: '6px 14px', borderRadius: '20px', color: 'white', fontWeight: 'bold' },
  sectionTitle: { color: '#2d3748', borderBottom: '2px solid #e2e8f0', paddingBottom: '8px' },
  table: { width: '100%', borderCollapse: 'collapse', marginBottom: '24px' },
  th: { background: '#edf2f7', padding: '10px', textAlign: 'left', fontSize: '13px', color: '#4a5568' },
  td: { padding: '10px', borderBottom: '1px solid #e2e8f0', fontSize: '14px' },
  analysisSection: { marginTop: '16px' },
  analyzeBtn: { padding: '12px 24px', background: '#6b46c1', color: 'white', border: 'none', borderRadius: '8px', fontSize: '15px', cursor: 'pointer' },
  analysisBox: { background: '#faf5ff', border: '1px solid #d6bcfa', borderRadius: '8px', padding: '16px' },
  analysisHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' },
  analysisTitle: { color: '#553c9a', margin: 0 },
  reanalyzeBtn: { padding: '6px 12px', background: '#2b6cb0', color: 'white', border: 'none', borderRadius: '6px', fontSize: '13px', cursor: 'pointer' },
  analysisText: { color: '#44337a', lineHeight: '1.6', whiteSpace: 'pre-wrap' },
  loading: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' },
};

export default ResultDetailPage;