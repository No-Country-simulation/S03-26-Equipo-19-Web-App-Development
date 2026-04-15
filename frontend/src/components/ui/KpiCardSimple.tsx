import type { ReactNode } from 'react';

interface KpiCardSimpleProps {
  icon: ReactNode;
  label: string;
  value: string;
  subtitle?: string;   // para el caso "etiqueta más usada"
  iconBg?: string;     // override del fondo del icono, default bg-blue-50
}

/**
 * KPI card para valores simples (strings) que no vienen como Metric del backend.
 * Para KPIs con changePercent/trend usar el KpiCard existente.
 */
export const KpiCardSimple = ({
  icon,
  label,
  value,
  subtitle,
  iconBg = 'bg-blue-50',
}: KpiCardSimpleProps) => (
  <div className="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4">
    <div className={`p-3 rounded-full ${iconBg} text-primary shrink-0`}>
      {icon}
    </div>
    <div className="min-w-0">
      <h3 className="text-sm font-semibold text-neutro-1 mb-1 truncate">{label}</h3>
      {value ? (
        <p className="text-3xl font-extrabold text-primary">{value}</p>
      ) : (
        <p className="text-base font-bold text-primary truncate">{subtitle}</p>
      )}
    </div>
  </div>
);