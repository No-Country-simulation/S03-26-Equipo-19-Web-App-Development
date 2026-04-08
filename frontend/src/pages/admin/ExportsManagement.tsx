import { Plus, FileText, Download, RefreshCw } from 'lucide-react';
import { Button } from '../../components/ui/Button';

// --- TIPOS ---
type ExportType = 'CSV' | 'PDF';
type ExportStatus = 'COMPLETADA' | 'EN PROCESO' | 'FALLIDA';

interface ExportItem {
  id: number;
  type: ExportType;
  entity: string;
  date: string;
  status: ExportStatus;
}

interface KpiCardProps {
  icon: React.ReactNode;
  label: string;
  value: string;
  trend: string;
  isPositive: boolean;
}

// --- MOCK DATA ---
const kpiData: KpiCardProps[] = [
  {
    icon: <FileText size={20} className="text-blue-600" />,
    label: 'Total de exportaciones',
    value: '15',
    trend: '+15.0%',
    isPositive: true,
  },
  {
    icon: <FileText size={20} className="text-blue-600" />,
    label: 'Exportaciones csv',
    value: '09',
    trend: '+4.8%',
    isPositive: true,
  },
  {
    icon: <FileText size={20} className="text-blue-600" />,
    label: 'Exportaciones pdf',
    value: '125',
    trend: '+1.5%',
    isPositive: true,
  },
  {
    icon: <FileText size={20} className="text-blue-600" />,
    label: 'Exportaciones completadas',
    value: '94%',
    trend: '+10.8%',
    isPositive: true,
  },
];

const mockExports: ExportItem[] = [
  { id: 1, type: 'CSV', entity: 'Contacts', date: '24 de octubre de 2023', status: 'COMPLETADA' },
  { id: 2, type: 'PDF', entity: 'Users', date: 'En este momento', status: 'EN PROCESO' },
  { id: 3, type: 'CSV', entity: 'Tasks', date: '12 de octubre de 2023', status: 'FALLIDA' },
  { id: 4, type: 'PDF', entity: 'Funnel_Stages', date: '10 de octubre de 2023', status: 'COMPLETADA' },
];

// --- SUBCOMPONENTES ---
const KpiCard = ({ icon, label, value, trend, isPositive }: KpiCardProps) => (
  <div className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm flex items-start gap-4">
    <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center shrink-0">
      {icon}
    </div>
    <div>
      <p className="text-slate-500 text-xs font-medium mb-1">{label}</p>
      <p className="text-2xl font-bold text-[#13316b]">{value}</p>
      <span className={`text-xs font-semibold ${isPositive ? 'text-green-500' : 'text-red-500'}`}>
        {trend}
      </span>
    </div>
  </div>
);

const TypeBadge = ({ type }: { type: ExportType }) => (
  <div className="flex items-center gap-2">
    <div className="w-8 h-8 bg-blue-50 rounded-lg flex items-center justify-center">
      <FileText size={15} className="text-blue-600" />
    </div>
    <span className="text-slate-700 font-medium text-sm">{type}</span>
  </div>
);

const StatusBadge = ({ status }: { status: ExportStatus }) => {
  const styles: Record<ExportStatus, string> = {
    'COMPLETADA': 'text-green-600',
    'EN PROCESO': 'text-yellow-500',
    'FALLIDA': 'text-red-500',
  };
  const dots: Record<ExportStatus, string> = {
    'COMPLETADA': 'bg-green-500',
    'EN PROCESO': 'bg-yellow-400',
    'FALLIDA': 'bg-red-400',
  };

  return (
    <span className={`flex items-center gap-1.5 text-xs font-semibold ${styles[status]}`}>
      <span className={`w-2 h-2 rounded-full ${dots[status]}`} />
      {status}
    </span>
  );
};

const ActionButton = ({ status }: { status: ExportStatus }) => {
  if (status === 'COMPLETADA') {
    return (
      <button className="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center text-blue-600 hover:bg-blue-100 transition-colors">
        <Download size={15} />
      </button>
    );
  }
  if (status === 'EN PROCESO') {
    return (
      <button className="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center text-slate-400 cursor-not-allowed">
        <RefreshCw size={15} className="animate-spin" />
      </button>
    );
  }
  return (
    <button className="w-8 h-8 rounded-lg bg-red-50 flex items-center justify-center text-red-400 hover:bg-red-100 transition-colors">
      <RefreshCw size={15} />
    </button>
  );
};

// --- PÁGINA PRINCIPAL ---
export const ExportsManagement = () => {
  return (
    <div>
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Gestión de Exportaciones</h1>
          <p className="text-slate-500 text-sm mt-0.5 max-w-sm">
            Organiza y controla tus datos. Guarda registros de relaciones en formatos descargables.
          </p>
        </div>
        <Button variant="primary" size="md" className="flex items-center gap-2 whitespace-nowrap">
          <Plus size={16} />
          Crear Exportación
        </Button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {kpiData.map((kpi, i) => (
          <KpiCard key={i} {...kpi} />
        ))}
      </div>

      {/* Tabla */}
      <div className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 text-slate-400 text-left text-xs uppercase tracking-wide">
                <th className="px-6 py-4 font-medium">Tipo de documento</th>
                <th className="px-6 py-4 font-medium">Entidad</th>
                <th className="px-6 py-4 font-medium">Fecha</th>
                <th className="px-6 py-4 font-medium">Estado</th>
                <th className="px-6 py-4 font-medium">Acción</th>
              </tr>
            </thead>
            <tbody>
              {mockExports.map(exp => (
                <tr key={exp.id} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                  <td className="px-6 py-4">
                    <TypeBadge type={exp.type} />
                  </td>
                  <td className="px-6 py-4 text-slate-600 font-medium">{exp.entity}</td>
                  <td className="px-6 py-4 text-slate-500">{exp.date}</td>
                  <td className="px-6 py-4">
                    <StatusBadge status={exp.status} />
                  </td>
                  <td className="px-6 py-4">
                    <ActionButton status={exp.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Footer */}
      <div className="mt-12 pt-6 border-t border-slate-200 flex flex-col sm:flex-row justify-between items-center gap-2 text-xs text-slate-400">
        <span>© 2026 Conversa CRM. Todos los derechos reservados.</span>
        <div className="flex gap-4">
          <button className="hover:text-slate-600 transition-colors">Política de Privacidad</button>
          <button className="hover:text-slate-600 transition-colors">Términos y Condiciones de Uso</button>
          <button className="hover:text-slate-600 transition-colors">Preguntas Frecuentes</button>
        </div>
      </div>
    </div>
  );
};