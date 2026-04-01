import { Bell } from 'lucide-react';

const alertsData = [
  {
    id: 1,
    title: '3 alerts: 2 alerts',
    description: 'Jorge R. - 2 days inactive',
    type: 'danger', // Rojo
  },
  {
    id: 2,
    title: '3 alerts: 3 alerts',
    description: 'Sara M. - High Interest',
    type: 'danger',
  },
  {
    id: 3,
    title: '3 alerts: 3 alerts',
    description: 'Alex P. - Awaiting Reply',
    type: 'info', // Azul claro
  },
];

export const AlertsList = () => {
  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm h-full flex flex-col">
      <h3 className="text-sm font-bold text-slate-700 mb-6">Alertas de Seguimiento</h3>
      
      <div className="flex flex-col gap-5">
        {alertsData.map((alert) => (
          <div key={alert.id} className="flex items-start gap-4">
            {/* Ícono dinámico según el tipo de alerta */}
            <div className={`p-2 rounded-full ${alert.type === 'danger' ? 'bg-rose-100 text-rose-500' : 'bg-sky-100 text-sky-500'}`}>
              <Bell size={18} fill="currentColor" className="opacity-80" />
            </div>
            
            {/* Textos */}
            <div>
              <p className="text-sm font-semibold text-slate-800">{alert.title}</p>
              <p className="text-sm text-slate-500 mt-0.5">{alert.description}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};