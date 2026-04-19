import { useState } from 'react';
import SpecAnalyzer from './components/SpecAnalyzer';
import CoverageAnalyzer from './components/CoverageAnalyzer';

type Tab = 'spec' | 'coverage';

export default function App() {
  const [tab, setTab] = useState<Tab>('spec');

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100">
      <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white font-bold text-sm">
              TF
            </div>
            <div>
              <h1 className="font-bold text-slate-100 leading-none">TestForge AI</h1>
              <p className="text-xs text-slate-500">API Test Intelligence Platform</p>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-6 py-8 space-y-8">
        <div>
          <h2 className="text-2xl font-bold text-slate-100">AI-Powered Test Intelligence</h2>
          <p className="text-slate-400 mt-1 text-sm">
            Upload your OpenAPI spec to generate risk-ranked test cases and detect coverage gaps — powered by Claude AI.
          </p>
        </div>

        <div className="flex gap-1 bg-slate-800 p-1 rounded-xl w-fit">
          {([
            { id: 'spec' as Tab, label: '🔬 Spec Analyzer' },
            { id: 'coverage' as Tab, label: '📊 Coverage Gaps' },
          ]).map(t => (
            <button
              key={t.id}
              onClick={() => setTab(t.id)}
              className={`px-5 py-2.5 rounded-lg text-sm font-medium transition-all ${
                tab === t.id
                  ? 'bg-indigo-600 text-white shadow'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>

        <div>
          {tab === 'spec' && <SpecAnalyzer />}
          {tab === 'coverage' && <CoverageAnalyzer />}
        </div>
      </div>
    </div>
  );
}
