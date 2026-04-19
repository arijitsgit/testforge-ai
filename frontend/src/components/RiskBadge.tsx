interface RiskBadgeProps {
  score: number;
}

export default function RiskBadge({ score }: RiskBadgeProps) {
  const color =
    score >= 8 ? 'bg-red-900/60 text-red-300 border-red-700' :
    score >= 5 ? 'bg-yellow-900/60 text-yellow-300 border-yellow-700' :
                 'bg-green-900/60 text-green-300 border-green-700';
  return (
    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-semibold border ${color}`}>
      Risk {score}/10
    </span>
  );
}
