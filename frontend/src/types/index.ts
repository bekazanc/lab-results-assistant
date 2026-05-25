export interface TestResult {
  id: number;
  name: string;
  value: number;
  unit: string;
  referenceMin: number;
  referenceMax: number;
  isAbnormal: boolean;
}

export interface LabResult {
  id: number;
  deviceId: string;
  patientId: string;
  timestamp: string;
  scenario: string;
  status: 'NORMAL' | 'ABNORMAL' | 'CRITICAL' | 'INVALID';
  createdAt: string;
  llmAnalysis?: string;
  tests: TestResult[];
}

export interface LabResultAnalysis {
  id: number;
  analysisText: string;
  createdAt: string;
}
