import type { ReactNode } from 'react';
import { ArrowUp, ArrowDown } from 'lucide-react';


type Color = "primary" | "secondary" | "success" | "error";

const colorMap: Record<Color, string> = {
  primary: "bg-primary/10 ",
  secondary: "bg-secondary/10",
  success: "bg-success/10",
  error: "bg-error/10 ",
};

interface KpiCardProps {
  title: string;
  value: string;
  trend: string;
  color: Color;
  isPositive: boolean;
  icon: ReactNode;
}

export const KpiCard = ({ title, value, trend, color, isPositive, icon }: KpiCardProps) => {


  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm flex items-center justify-between">
      
      <div className="flex items-center gap-5">
        <div className={`p-4 rounded-full ${colorMap[color]} text-primary`}>
          {icon}
        </div>
        <div>         
          <h3 className="text-sm font-semibold text-neutro-1 mb-1">{title}</h3>
          <p className="text-3xl font-extrabold text-primary">{value}</p>
        </div>
      </div>

      <div className="flex flex-col items-end">
        <span className={`flex items-center gap-1 text-xs font-bold ${isPositive ? 'text-emerald-500' : 'text-rose-500'}`}>
          {isPositive ? <ArrowUp size={16} strokeWidth={3} /> : <ArrowDown size={16} strokeWidth={3} />}
          {trend}
        </span>
        <span className="text-xs text-neutro-2 font-medium mt-1 text-right">en el último mes</span>
      </div>
      
    </div>
  );
};