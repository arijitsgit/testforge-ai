import { useState } from 'react';
import { analyzeSpec } from '../api';
import type { AnalysisResult, EndpointAnalysis } from '../types';
import FileUpload from './FileUpload';
import RiskBadge from './RiskBadge';
import Badge from './Badge';
import TestCaseCard from './TestCaseCard';

export default function SpecAnalyzer() {
  const [file, setFile] = useState<File | null>(null);
  const [patternFile, setPatternFile] = useState<File | null>(null);
  const [githubUrl, setGithubUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<AnalysisResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<EndpointAnalysis | null>(null);
  const [baseUrl, setBaseUrl] = useState('http://localhost:8080');

  const handleAnalyze = async () => {
    if (!file) return;
    setLoading(true);
    setError(null);
    try {
      const data = await analyzeSpec(file);
      setResult(data);
      setSelected(data.endpoints[0] ?? null);
    } catch {
      setError('Analysis failed. Check the spec file and ensure the backend is running.');
    } finally {
      setLoading(false);
    }
  };

  const methodColor: Record<string, string> = {
    GET: 'text-green-400', POST: 'text-blue-400',
    PUT: 'text-yellow-400', DELETE: 'text-red-400', PATCH: 'text-purple-400',
  };

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="md:col-span-2">
          <FileUpload label="Drop your OpenAPI spec here" onFile={setFile} />
        </div>
        <div className="space-y-3">
          <div>
            <label className="block text-xs text-slate-400 mb-1">Base URL (for generated tests)</label>
            <input
              value={baseUrl}
              onChange={e => setBaseUrl(e.target.value)}
              className="w-full bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-indigo-500"
            />
          </div>
          <div>
            <label className="block text-xs text-slate-400 mb-1">
              GitHub automation repo URL <span className="text-slate-600">(optional — generated tests will match your framework)</span>
            </label>
            <input
              value={githubUrl}
              onChange={e => setGithubUrl(e.target.value)}
              placeholder="https://github.com/your-org/your-test-repo"
              className="w-full bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-indigo-500 placeholder-slate-600"
            />
            {githubUrl && (
              <p className="text-xs text-indigo-400 mt-1">✓ Claude will read your test files and match their style</p>
            )}
          </div>
          {!githubUrl && (
            <FileUpload
              label="Or drop an existing test file to match its pattern"
              accept=".java,.kt,.groovy,.py,.js,.ts"
              onFile={setPatternFile}
            />
          )}
          {patternFile && !githubUrl && (
            <div className="bg-indigo-900/30 border border-indigo-700 rounded-lg px-3 py-2 text-xs text-indigo-300">
              ✓ Pattern: <span className="font-mono">{patternFile.name}</span> — generated code will match your team's style
            </div>
          )}
          <button
            onClick={handleAnalyze}
            disabled={!file || loading}
            className="w-full py-3 rounded-lg bg-indigo-600 hover:bg-indigo-500 disabled:opacity-40 disabled:cursor-not-allowed font-semibold transition-colors"
          >
            {loading ? '🤖 Analyzing...' : '🚀 Analyze Spec'}
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-red-900/30 border border-red-700 rounded-lg p-4 text-red-300 text-sm">{error}</div>
      )}

      {result && (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[
              { label: 'API Title', value: result.specTitle },
              { label: 'Version', value: result.specVersion },
              { label: 'Endpoints', value: result.totalEndpoints },
              { label: 'Test Cases', value: result.totalTestCases },
            ].map(stat => (
              <div key={stat.label} className="bg-slate-800 rounded-xl p-4 border border-slate-700">
                <p className="text-slate-400 text-xs mb-1">{stat.label}</p>
                <p className="text-xl font-bold text-slate-100">{stat.value}</p>
              </div>
            ))}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="space-y-2">
              <h3 className="text-sm font-semibold text-slate-400 uppercase tracking-wide">Endpoints</h3>
              {result.endpoints.map((ea, i) => (
                <button
                  key={i}
                  onClick={() => setSelected(ea)}
                  className={`w-full text-left rounded-lg p-3 border transition-colors ${
                    selected === ea
                      ? 'bg-indigo-900/40 border-indigo-600'
                      : 'bg-slate-800 border-slate-700 hover:border-slate-500'
                  }`}
                >
                  <div className="flex items-center justify-between gap-2">
                    <div className="flex items-center gap-2 min-w-0">
                      <span className={`text-xs font-mono font-bold ${methodColor[ea.endpoint.method] ?? 'text-slate-300'}`}>
                        {ea.endpoint.method}
                      </span>
                      <span className="text-xs text-slate-300 truncate">{ea.endpoint.path}</span>
                    </div>
                    <RiskBadge score={ea.riskScore} />
                  </div>
                  {ea.endpoint.summary && (
                    <p className="text-xs text-slate-500 mt-1 truncate">{ea.endpoint.summary}</p>
                  )}
                </button>
              ))}
            </div>

            <div className="lg:col-span-2 space-y-4">
              {selected && (
                <>
                  <div className="flex items-center gap-3">
                    <span className={`font-mono font-bold ${methodColor[selected.endpoint.method] ?? ''}`}>
                      {selected.endpoint.method}
                    </span>
                    <span className="text-slate-200 font-mono">{selected.endpoint.path}</span>
                    <RiskBadge score={selected.riskScore} />
                    <Badge label={`${selected.testCases.length} test cases`} color="blue" />
                  </div>
                  <div className="space-y-3">
                    {selected.testCases.map(tc => (
                      <TestCaseCard key={tc.id} testCase={tc} baseUrl={baseUrl} patternFile={patternFile ?? undefined} githubUrl={githubUrl || undefined} />
                    ))}
                  </div>
                </>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
