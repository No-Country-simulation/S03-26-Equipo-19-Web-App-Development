import { LineChart as LineChartIcon } from "lucide-react";
import { cn } from "../../lib/utils";
import { LineChartData } from "../../types";

interface LineChartProps {
  data: LineChartData[];
  title: string;
  className?: string;
}

export function LineChart({ data, title, className }: LineChartProps) {
  return (
    <div
      className={cn(
        "bg-white p-6 rounded-[2rem] border border-slate-100 shadow-sm hover:shadow-lg transition-all duration-300 group",
        className,
      )}
    >
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-gradient-to-br from-emerald-500 to-emerald-600 rounded-xl shadow-lg shadow-emerald-500/20 group-hover:scale-110 transition-transform">
            <LineChartIcon size={18} className="text-white" />
          </div>
          <h3 className="font-bold text-slate-900">{title}</h3>
        </div>
        <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-1 rounded-full">
          +8.3%
        </span>
      </div>
      <div className="relative h-32">
        <svg
          className="w-full h-full"
          viewBox="0 0 300 100"
          preserveAspectRatio="none"
        >
          <defs>
            <linearGradient id="lineGradient" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#10b981" stopOpacity="0.4" />
              <stop offset="100%" stopColor="#10b981" stopOpacity="0" />
            </linearGradient>
          </defs>
          <path
            d={
              data
                .map(
                  (p, i) =>
                    `${i === 0 ? "M" : "L"} ${(i / (data.length - 1)) * 300} ${100 - p.value}`,
                )
                .join(" ") + " L 300 100 L 0 100 Z"
            }
            fill="url(#lineGradient)"
          />
          <path
            d={data
              .map(
                (p, i) =>
                  `${i === 0 ? "M" : "L"} ${(i / (data.length - 1)) * 300} ${100 - p.value}`,
              )
              .join(" ")}
            fill="none"
            stroke="#10b981"
            strokeWidth="3"
            strokeLinecap="round"
            strokeLinejoin="round"
            className="drop-shadow-lg"
          />
        </svg>
      </div>
      <div className="flex justify-between mt-3 text-[10px] text-slate-400 font-medium">
        <span>Ene</span>
        <span>Feb</span>
        <span>Mar</span>
        <span>Abr</span>
        <span>May</span>
        <span>Jun</span>
      </div>
    </div>
  );
}
