import React from "react";
import {
  Users,
  Clock,
  CheckCircle2,
  ArrowUpRight,
  TrendingUp,
} from "lucide-react";
import { Stats } from "../../types";

interface KPIGridProps {
  stats: Stats;
}

const KPIGrid: React.FC<KPIGridProps> = ({ stats }) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 mb-10">
      <div className="bg-white p-7 rounded-[2rem] border border-slate-100 shadow-md hover:shadow-2xl hover:-translate-y-1 transition-all duration-300 group relative overflow-hidden">
        <div className="absolute top-0 right-0 p-6 opacity-5 group-hover:scale-150 transition-transform duration-500">
          <Users size={100} />
        </div>
        <div className="relative">
          <div className="text-xs font-bold text-indigo-500 uppercase tracking-widest mb-2">
            Total Leads
          </div>
          <div className="text-4xl font-black text-slate-900 group-hover:text-indigo-600 transition-colors">
            {stats.total}
          </div>
          <div className="mt-3 flex items-center gap-2 text-xs font-bold text-emerald-600">
            <span className="flex items-center gap-1 px-2 py-1 bg-emerald-50 rounded-full">
              <ArrowUpRight size={14} /> +12% este mes
            </span>
          </div>
        </div>
      </div>
      <div className="bg-white p-7 rounded-[2rem] border border-slate-100 shadow-md hover:shadow-2xl hover:-translate-y-1 transition-all duration-300 group relative overflow-hidden">
        <div className="absolute top-0 right-0 p-6 opacity-5 group-hover:scale-150 transition-transform duration-500">
          <Clock size={100} />
        </div>
        <div className="relative">
          <div className="text-xs font-bold text-amber-500 uppercase tracking-widest mb-2">
            Pendientes
          </div>
          <div className="text-4xl font-black text-amber-500 group-hover:scale-110 transition-transform">
            {stats.active}
          </div>
          <div className="mt-3 flex items-center gap-2 text-xs font-bold text-slate-400">
            <span className="px-2 py-1 bg-slate-50 rounded-full">
              Requieren atención
            </span>
          </div>
        </div>
      </div>
      <div className="bg-white p-7 rounded-[2rem] border border-slate-100 shadow-md hover:shadow-2xl hover:-translate-y-1 transition-all duration-300 group relative overflow-hidden">
        <div className="absolute top-0 right-0 p-6 opacity-5 group-hover:scale-150 transition-transform duration-500">
          <CheckCircle2 size={100} />
        </div>
        <div className="relative">
          <div className="text-xs font-bold text-emerald-500 uppercase tracking-widest mb-2">
            Cerrados
          </div>
          <div className="text-4xl font-black text-emerald-500 group-hover:scale-110 transition-transform">
            {stats.closed}
          </div>
          <div className="mt-3 flex items-center gap-2 text-xs font-bold text-emerald-600">
            <span className="flex items-center gap-1 px-2 py-1 bg-emerald-50 rounded-full">
              <TrendingUp size={14} /> Eficiencia alta
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default KPIGrid;
