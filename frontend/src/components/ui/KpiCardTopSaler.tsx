// src/components/ui/KpiCardTopSaler.tsx
import { Award, Star } from 'lucide-react';
import type { TopSalesPerson } from '../../types/metric.types';

interface KpiCardTopSalerProps {
  person: TopSalesPerson;
}

export const KpiCardTopSaler = ({ person }: KpiCardTopSalerProps) => {
  return (
    <div className="bg-gradient-to-br from-amber-50 to-yellow-50 p-5 rounded-2xl border border-amber-100 shadow-sm">
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center">
            <Award size={20} className="text-amber-600" />
          </div>
          <div>
            <p className="text-xs text-amber-600 font-semibold">🏆 Mejor Vendedor</p>
            <p className="text-lg font-bold text-[#13316b]">{person.name}</p>
            <p className="text-xs text-slate-500">{person.email}</p>
          </div>
        </div>
        <div className="text-right">
          <div className="flex items-center gap-1">
            <Star size={14} className="text-yellow-500 fill-yellow-500" />
            <span className="text-sm font-bold text-amber-600">{person.performanceScore}</span>
          </div>
        </div>
      </div>
      <div className="mt-3 pt-3 border-t border-amber-100 flex justify-between">
        <div>
          <p className="text-2xl font-bold text-[#13316b]">{person.messagesSent}</p>
          <p className="text-xs text-slate-500">mensajes enviados</p>
        </div>
      </div>
    </div>
  );
};