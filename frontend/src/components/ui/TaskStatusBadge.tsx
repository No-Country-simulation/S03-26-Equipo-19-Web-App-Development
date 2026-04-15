interface TaskStatusBadgeProps {
  type: string;
}

const STYLES: Record<string, string> = {
  'follow-up': 'bg-blue-50 text-blue-600',
  'reminder':  'bg-yellow-50 text-yellow-600',
  'pending':   'bg-orange-50 text-orange-500',
  'done':      'bg-green-50 text-green-600',
};

const LABELS: Record<string, string> = {
  'follow-up': 'Seguimiento',
  'reminder':  'Recordatorio',
  'pending':   'Pendiente',
  'done':      'Completada',
};

export const TaskStatusBadge = ({ type }: TaskStatusBadgeProps) => (
  <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${STYLES[type] ?? 'bg-slate-100 text-slate-500'}`}>
    {LABELS[type] ?? type}
  </span>
);