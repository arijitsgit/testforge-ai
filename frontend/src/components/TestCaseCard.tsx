import { useState } from 'react';
import type { TestCase } from '../types';
import { generateTestCode } from '../api';
import Badge from './Badge';
import RiskBadge from './RiskBadge';

const categoryColor: Record<string, 'green' | 'blue' | 'red' | 'yellow' | 'purple'> = {
  HAPPY_PATH:   'green',
  BOUNDARY:     'blue',
  AUTH_FAILURE: 'red',
  ERROR:        'yellow',
  VALIDATION:   'purple',
};

interface TestCaseCardProps {
  testCase: TestCase;
  baseUrl?: string;
}

export default function TestCaseCard({ testCase, baseUrl }: TestCaseCardProps) {
  const [code, setCode] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleGenerate = async () => {
    setLoading(true);
    try {
      const result = await generateTestCode(testCase, baseUrl);
      setCode(result);
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = () => {
    if (code) {
      navigator.clipboard.writeText(code);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="bg-slate-800 rounded-lg border border-slate-700 p-4 space-y-3">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="font-semibold text-slate-100 text-sm">{testCase.name}</p>
          <p className="text-slate-400 text-xs mt-0.5">{testCase.description}</p>
        </div>
        <div className="flex gap-2 flex-shrink-0">
          <Badge label={testCase.category} color={categoryColor[testCase.category] ?? 'gray'} />
          <RiskBadge score={testCase.riskScore} />
        </div>
      </div>

      <div className="flex items-center gap-4 text-xs text-slate-400">
        <span>Expected: <span className="text-slate-200 font-mono">{testCase.expectedStatusCode}</span></span>
        {testCase.requestBody && (
          <span>Body: <span className="text-slate-200 font-mono truncate max-w-[180px]">{testCase.requestBody}</span></span>
        )}
      </div>

      {!code && (
        <button
          onClick={handleGenerate}
          disabled={loading}
          className="w-full py-1.5 px-3 text-xs rounded bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 transition-colors font-medium"
        >
          {loading ? 'Generating...' : '⚡ Generate JUnit Code'}
        </button>
      )}

      {code && (
        <div className="relative">
          <pre className="text-xs bg-slate-900 rounded p-3 overflow-x-auto max-h-64 text-emerald-300 border border-slate-700">
            <code>{code}</code>
          </pre>
          <button
            onClick={handleCopy}
            className="absolute top-2 right-2 text-xs px-2 py-1 rounded bg-slate-700 hover:bg-slate-600 transition-colors"
          >
            {copied ? '✓ Copied' : 'Copy'}
          </button>
        </div>
      )}
    </div>
  );
}
