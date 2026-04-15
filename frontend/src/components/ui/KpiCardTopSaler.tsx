import type { TopSalesPerson } from '../../types/metric.types';


export const KpiCardTopSaler = ({ person }: { person: TopSalesPerson }) => {
  return (
    <div>
      <p className="text-lg font-bold text-primary">{person.name}</p>
      <p className="text-xs text-neutro-2">{person.email}</p>

      <div className="mt-2 text-sm">
        <p>📩 {person.messagesSent} mensajes</p>
        <p>⭐ {person.performanceScore} pts</p>
      </div>
    </div>
  );
};


