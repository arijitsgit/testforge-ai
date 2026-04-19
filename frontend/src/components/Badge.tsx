interface BadgeProps {
  label: string;
  color?: 'red' | 'yellow' | 'green' | 'blue' | 'purple' | 'gray';
}

const colorMap: Record<string, string> = {
  red:    'bg-red-900/50 text-red-300 border border-red-700',
  yellow: 'bg-yellow-900/50 text-yellow-300 border border-yellow-700',
  green:  'bg-green-900/50 text-green-300 border border-green-700',
  blue:   'bg-blue-900/50 text-blue-300 border border-blue-700',
  purple: 'bg-purple-900/50 text-purple-300 border border-purple-700',
  gray:   'bg-slate-700 text-slate-300 border border-slate-600',
};

export default function Badge({ label, color = 'gray' }: BadgeProps) {
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${colorMap[color]}`}>
      {label}
    </span>
  );
}
