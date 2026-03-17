import type { ReactNode } from 'react';
import { ArrowUp, ArrowDown } from 'lucide-react';

interface KpiCardProps {
  title: string;
  value: string;
  trend: string;
  isPositive: boolean;
  icon: ReactNode;
}

export const KpiCard = ({ title, value, trend, isPositive, icon }: KpiCardProps) => {
  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm flex items-center justify-between">
      
      <div className="flex items-center gap-5">
        <div className="p-4 bg-[#f0f6ff] text-[#13316b] rounded-full">
          {icon}
        </div>
        <div>
          {/* Título más sutil */}
          <h3 className="text-sm font-semibold text-slate-500 mb-1">{title}</h3>
          {/* Número más fuerte y oscuro */}
          <p className="text-3xl font-extrabold text-[#13316b]">{value}</p>
        </div>
      </div>

      <div className="flex flex-col items-end">
        <span className={`flex items-center gap-1 text-sm font-bold ${isPositive ? 'text-emerald-500' : 'text-rose-500'}`}>
          {isPositive ? <ArrowUp size={16} strokeWidth={3} /> : <ArrowDown size={16} strokeWidth={3} />}
          {trend}
        </span>
        <span className="text-xs text-slate-400 font-medium mt-1">vs last month</span>
      </div>
      
    </div>
  );
};