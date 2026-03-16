import { PieChart as PieChartIcon } from "lucide-react";
import { cn } from "../../lib/utils";
import { PieChartData } from "../../types";

interface PieChartProps {
  data: PieChartData[];
  title: string;
  colors: string[];
  className?: string;
}

interface PieSegment {
  percent: number;
  start: number;
  dashArray: string;
  dashOffset: number;
  label: string;
  value: number;
}

export function PieChart({ data, title, colors, className }: PieChartProps) {
  const segments: PieSegment[] = data.reduce((acc: PieSegment[], item) => {
    const prev = acc.reduce((sum, d) => sum + d.percent, 0);
    const dashArray = `${item.percent} ${100 - item.percent}`;
    const dashOffset = -prev;
    acc.push({ ...item, start: prev, dashArray, dashOffset });
    return acc;
  }, []);

  return (
    <div
      className={cn(
        "bg-white p-6 rounded-[2rem] border border-slate-100 shadow-sm hover:shadow-lg transition-all duration-300 group",
        className,
      )}
    >
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-gradient-to-br from-purple-500 to-purple-600 rounded-xl shadow-lg shadow-purple-500/20 group-hover:scale-110 transition-transform">
            <PieChartIcon size={18} className="text-white" />
          </div>
          <h3 className="font-bold text-slate-900">{title}</h3>
        </div>
      </div>
      <div className="flex items-center gap-6">
        <div className="relative w-28 h-28">
          <svg viewBox="0 0 36 36" className="w-full h-full -rotate-90">
            {segments.map((item, idx) => (
              <circle
                key={idx}
                cx="18"
                cy="18"
                r="15.9155"
                fill="transparent"
                stroke={colors[idx]}
                strokeWidth="4"
                strokeDasharray={item.dashArray}
                strokeDashoffset={item.dashOffset}
                className="transition-all duration-700 hover:stroke-width-6 cursor-pointer"
              />
            ))}
          </svg>
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="text-lg font-black text-slate-900">100%</div>
          </div>
        </div>
        <div className="flex-1 space-y-3">
          {data.map((item, idx) => (
            <div
              key={idx}
              className="flex items-center justify-between group/item"
            >
              <div className="flex items-center gap-2">
                <div
                  className="w-3 h-3 rounded-full shadow-md"
                  style={{ backgroundColor: colors[idx] }}
                />
                <span className="text-xs font-medium text-slate-600 group-hover/item:text-slate-900 transition-colors">
                  {item.label}
                </span>
              </div>
              <span className="text-xs font-bold text-slate-900">
                {item.value}%
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
