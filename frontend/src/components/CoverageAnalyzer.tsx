import { useState } from 'react';
import { analyzeCoverage } from '../api';
import type { CoverageGapResult, EndpointAnalysis } from '../types';
import FileUpload from './FileUpload';
import RiskBadge from './RiskBadge';
import TestCaseCard from './TestCaseCard';

export default function CoverageAnalyzer() {
  const [specFile, setSpecFile] = useState<File | null>(null);
  const [testFile, setTestFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<CoverageGapResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<EndpointAnalysis | null>(null);

  const handleAnalyze = async () => {
    if (!specFile || !testFile) return;
    setLoading(true);
    setError(null);
    try {
      const data = await analyzeCoverage(specFile, testFile);
      setResult(data);
      const firstGap = data.endpoints.find(e => e.coverageStatus !== 'COVERED') ?? data.endpoints[0];
      setSelected(firstGap ?? null);
    } catch {
      setError('Coverage analysis failed. Check both files and ensure the backend is running.');
    } finally {
      setLoading(false);
    }
  };

  const statusColor: Record<string, string> = {
    COVERED: 'bg-green-900/40 border-green-700',
    PARTIAL:  'bg-yellow-900/40 border-yellow-700',
    NONE:     'bg-red-900/40 border-red-700',
  };

  const statusLabel: Record<string, string> = {
    COVERED: '✓ Covered',
    PARTIAL: '~ Partial',
    NONE:    '✗ No Tests',
  };

  const methodColor: Record<string, string> = {
    GET: 'text-green-400', POST: 'text-blue-400',
    PUT: 'text-yellow-400', DELETE: 'text-red-400', PATCH: 'text-purple-400',
  };

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <FileUpload label="Drop OpenAPI spec" onFile={setSpecFile} />
        <FileUpload label="Drop existing test file (.java)" accept=".java,.kt,.groovy" onFile={setTestFile} />
      </div>

      <button
        onClick={handleAnalyze}
        disabled={!specFile || !testFile || loading}
        className="w-full py-3 rounded-lg bg-indigo-600 hover:bg-indigo-500 disabled:opacity-40 disabled:cursor-not-allowed font-semibold transition-colors"
      >
        {loading ? '🔍 Detecting Gaps...' : '🔍 Detect Coverage Gaps'}
      </button>

      {error && (
        <div className="bg-red-900/30 border border-red-700 rounded-lg p-4 text-red-300 text-sm">{error}</div>
      )}

      {result && (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[
              { label: 'Total Endpoints', value: result.totalEndpoints, cls: 'text-slate-100' },
              { label: 'Covered', value: result.coveredEndpoints, cls: 'text-green-400' },
              { label: 'Partial', value: result.partialEndpoints, cls: 'text-yellow-400' },
              { label: 'No Tests', value: result.uncoveredEndpoints, cls: 'text-red-400' },
            ].map(s => (
              <div key={s.label} className="bg-slate-800 rounded-xl p-4 border border-slate-700">
                <p className="text-slate-400 text-xs mb-1">{s.label}</p>
                <p className={`text-2xl font-bold ${s.cls}`}>{s.value}</p>
              </div>
            ))}
          </div>

          <div className="bg-slate-800 rounded-xl border border-slate-700 p-4">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm text-slate-400">Overall Coverage</span>
              <span className="font-bold text-slate-100">{result.coveragePercent.toFixed(1)}%</span>
            </div>
            <div className="h-3 bg-slate-700 rounded-full overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-indigo-600 to-emerald-500 rounded-full transition-all"
                style={{ width: `${result.coveragePercent}%` }}
              />
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="space-y-2">
              <h3 className="text-sm font-semibold text-slate-400 uppercase tracking-wide">Coverage Heatmap</h3>
              {result.endpoints.map((ea, i) => (
                <button
                  key={i}
                  onClick={() => setSelected(ea)}
                  className={`w-full text-left rounded-lg p-3 border transition-all ${
                    selected === ea ? 'ring-2 ring-indigo-500 ' : ''
                  }${statusColor[ea.coverageStatus]}`}
                >
                  <div className="flex items-center justify-between gap-2">
                    <div className="flex items-center gap-2 min-w-0">
                      <span className={`text-xs font-mono font-bold ${methodColor[ea.endpoint.method] ?? ''}`}>
                        {ea.endpoint.method}
                      </span>
                      <span className="text-xs text-slate-300 truncate">{ea.endpoint.path}</span>
                    </div>
                    <span className="text-xs font-medium flex-shrink-0">
                      {statusLabel[ea.coverageStatus]}
                    </span>
                  </div>
                  <div className="flex items-center gap-2 mt-1">
                    <RiskBadge score={ea.riskScore} />
                    {ea.testCases.length > 0 && (
                      <span className="text-xs text-slate-400">{ea.testCases.length} gaps found</span>
                    )}
                  </div>
                </button>
              ))}
            </div>

            <div className="lg:col-span-2 space-y-4">
              {selected && selected.testCases.length > 0 && (
                <>
                  <h3 className="font-semibold text-slate-200">
                    Missing test cases for{' '}
                    <span className={`font-mono ${methodColor[selected.endpoint.method] ?? ''}`}>
                      {selected.endpoint.method}
                    </span>{' '}
                    <span className="font-mono text-slate-300">{selected.endpoint.path}</span>
                  </h3>
                  <div className="space-y-3">
                    {selected.testCases.map(tc => (
                      <TestCaseCard key={tc.id} testCase={tc} />
                    ))}
                  </div>
                </>
              )}
              {selected && selected.testCases.length === 0 && (
                <div className="flex items-center justify-center h-40 text-emerald-400 font-medium">
                  ✓ Fully covered — no gaps detected
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
