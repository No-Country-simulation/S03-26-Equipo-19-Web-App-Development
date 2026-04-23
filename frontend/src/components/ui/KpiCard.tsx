import type { ReactNode } from 'react';
import { ArrowUp, ArrowDown, Minus } from 'lucide-react';

type Color = "primary" | "secondary" | "success" | "error";
type Trend = 'up' | 'down' | 'stable';

export interface Metric {
  value: number;
  changePercent: number;
  trend: Trend;
}

const colorMap: Record<Color, string> = {
  primary: "bg-primary/10",
  secondary: "bg-secondary/10",
  success: "bg-success/10",
  error: "bg-error/10",
};

interface KpiCardProps {
  title: string;
  metric: Metric;
  color: Color;
  icon: ReactNode;
  impact?: 'positive' | 'negative';
}


export const KpiCard = ({
  title,
  metric,
  color,
  icon,
  impact = 'positive',
}: KpiCardProps) => {
  const { value, changePercent, trend } = metric;

  const isGood =
    (trend === 'up' && impact === 'positive') ||
    (trend === 'down' && impact === 'negative');

  const trendConfig = {
    up: {
      icon: <ArrowUp size={16} strokeWidth={3} />,
      text: `${changePercent}%`,
    },
    down: {
      icon: <ArrowDown size={16} strokeWidth={3} />,
      text: `${changePercent}%`,
    },
    stable: {
      icon: <Minus size={16} strokeWidth={3} />,
      text: '-',
    },
  };

  const currentTrend = trendConfig[trend];

  const trendColor =
    trend === 'stable'
      ? 'text-primary'
      : isGood
        ? 'text-success'
        : 'text-error';

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
        <span className={`flex items-center gap-1 text-xs font-bold ${trendColor}`}>
          {currentTrend.icon}
          {currentTrend.text}
        </span>
        <span className="text-xs text-neutro-2 font-medium mt-1 text-right">
          en el último mes
        </span>
      </div>

    </div>
  );
};