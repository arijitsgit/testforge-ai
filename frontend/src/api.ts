import axios from 'axios';
import type { AnalysisResult, CoverageGapResult, TestCase } from './types';

const BASE = '/api';

export async function analyzeSpec(file: File): Promise<AnalysisResult> {
  const form = new FormData();
  form.append('file', file);
  const { data } = await axios.post<AnalysisResult>(`${BASE}/analyze/spec`, form);
  return data;
}

export async function analyzeCoverage(specFile: File, testFile: File): Promise<CoverageGapResult> {
  const form = new FormData();
  form.append('spec', specFile);
  form.append('tests', testFile);
  const { data } = await axios.post<CoverageGapResult>(`${BASE}/analyze/coverage`, form);
  return data;
}

export async function generateTestCode(
  testCase: TestCase,
  baseUrl?: string,
  patternFile?: File,
  githubUrl?: string
): Promise<string> {
  const form = new FormData();
  form.append('testCase', JSON.stringify(testCase));
  if (baseUrl) form.append('baseUrl', baseUrl);
  if (githubUrl) form.append('githubUrl', githubUrl);
  else if (patternFile) form.append('pattern', patternFile);
  const { data } = await axios.post<{ code: string }>(`${BASE}/generate/test-code`, form);
  return data.code;
}
