import { BarChart3 } from "lucide-react";
import { cn } from "../../lib/utils";
import { ChartData } from "../../types";

interface BarChartProps {
  data: ChartData[];
  title: string;
  className?: string;
}

export function BarChart({ data, title, className }: BarChartProps) {
  return (
    <div
      className={cn(
        "bg-white p-6 rounded-[2rem] border border-slate-100 shadow-sm hover:shadow-lg transition-all duration-300 group",
        className,
      )}
    >
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl shadow-lg shadow-blue-500/20 group-hover:scale-110 transition-transform">
            <BarChart3 size={18} className="text-white" />
          </div>
          <h3 className="font-bold text-slate-900">{title}</h3>
        </div>
        <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-1 rounded-full">
          +12%
        </span>
      </div>
      <div className="flex items-end justify-between h-40 gap-3">
        {data.map((item, idx) => (
          <div
            key={idx}
            className="flex flex-col items-center flex-1 group/bar"
          >
            <div
              className="w-full bg-gradient-to-t from-blue-600 to-blue-400 rounded-t-lg transition-all duration-700 hover:from-indigo-600 hover:to-purple-500 group-hover/bar:scale-y-110 origin-bottom"
              style={{ height: `${item.value}%` }}
            />
            <span className="text-[10px] font-medium text-slate-400 mt-3 group-hover/bar:text-slate-600 transition-colors">
              {item.label}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
