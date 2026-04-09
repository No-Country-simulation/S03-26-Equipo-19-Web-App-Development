import { useState } from 'react';
import { UserPlus, Download, Filter, Pencil, Trash2, Users, MessageSquare, TrendingUp, UserCheck } from 'lucide-react';
import { Button } from '../../components/ui/Button';

// --- TIPOS ---
type SalespersonStatus = 'Active' | 'Inactive';

interface Salesperson {
  id: number;
  name: string;
  email: string;
  avatar: string;
  status: SalespersonStatus;
  assignedContacts: number;
  messagesSent: number;
  responseRate: number;
  lastActivity: string;
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
    icon: <Users size={20} className="text-blue-600" />,
    label: 'Total de vendedores',
    value: '12',
    trend: '+2 este mes',
    isPositive: true,
  },
  {
    icon: <UserCheck size={20} className="text-blue-600" />,
    label: 'Vendedores activos',
    value: '9',
    trend: '+1 esta semana',
    isPositive: true,
  },
  {
    icon: <MessageSquare size={20} className="text-blue-600" />,
    label: 'Mensajes enviados',
    value: '3,541',
    trend: '+12.4%',
    isPositive: true,
  },
  {
    icon: <TrendingUp size={20} className="text-blue-600" />,
    label: 'Tasa de respuesta promedio',
    value: '74%',
    trend: '+5.2%',
    isPositive: true,
  },
];

const mockSalespersons: Salesperson[] = [
  {
    id: 1,
    name: 'Sara Martínez',
    email: 'sara.m@conversa.com',
    avatar: 'SM',
    status: 'Active',
    assignedContacts: 48,
    messagesSent: 1402,
    responseRate: 88,
    lastActivity: 'hace 2 minutos',
  },
  {
    id: 2,
    name: 'Marcos Nodal',
    email: 'marcos.n@conversa.com',
    avatar: 'MN',
    status: 'Active',
    assignedContacts: 35,
    messagesSent: 1190,
    responseRate: 80,
    lastActivity: 'hace 45 minutos',
  },
  {
    id: 3,
    name: 'Elena Rodríguez',
    email: 'elena.r@conversa.com',
    avatar: 'ER',
    status: 'Inactive',
    assignedContacts: 22,
    messagesSent: 954,
    responseRate: 35,
    lastActivity: 'hace 3 días',
  },
  {
    id: 4,
    name: 'Julian Álvarez',
    email: 'julian.a@conversa.com',
    avatar: 'JA',
    status: 'Active',
    assignedContacts: 41,
    messagesSent: 1087,
    responseRate: 76,
    lastActivity: 'hace 1 hora',
  },
  {
    id: 5,
    name: 'Pedro Fernández',
    email: 'pedro.f@conversa.com',
    avatar: 'PF',
    status: 'Active',
    assignedContacts: 29,
    messagesSent: 873,
    responseRate: 68,
    lastActivity: 'ayer',
  },
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

const StatusDot = ({ status }: { status: SalespersonStatus }) => (
  <span className="flex items-center gap-1.5 text-sm">
    <span className={`w-2 h-2 rounded-full ${status === 'Active' ? 'bg-green-500' : 'bg-red-400'}`} />
    <span className={status === 'Active' ? 'text-green-600' : 'text-red-500'}>{status}</span>
  </span>
);

const ResponseBar = ({ rate }: { rate: number }) => (
  <div className="flex items-center gap-2">
    <div className="w-20 bg-slate-100 rounded-full h-1.5">
      <div
        className={`h-1.5 rounded-full ${rate >= 70 ? 'bg-green-500' : rate >= 50 ? 'bg-yellow-400' : 'bg-red-400'}`}
        style={{ width: `${rate}%` }}
      />
    </div>
    <span className="text-xs text-slate-600">{rate}%</span>
  </div>
);

// --- PÁGINA PRINCIPAL ---
export const SalespersonsManagement = () => {
  const [salespersons, setSalespersons] = useState<Salesperson[]>(mockSalespersons);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | 'Active' | 'Inactive'>('all');
  const [currentPage, setCurrentPage] = useState(1);

  const filtered = salespersons.filter(s => {
    const matchSearch =
      s.name.toLowerCase().includes(search.toLowerCase()) ||
      s.email.toLowerCase().includes(search.toLowerCase());
    const matchStatus = statusFilter === 'all' || s.status === statusFilter;
    return matchSearch && matchStatus;
  });

  const handleDelete = (id: number) => {
    setSalespersons(prev => prev.filter(s => s.id !== id));
  };

  return (
    <div>
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Gestión de Vendedores</h1>
          <p className="text-slate-500 text-sm mt-0.5">
            Administrá el equipo de ventas, asigná contactos y monitoreá el rendimiento
          </p>
        </div>
        <Button variant="primary" size="md" className="flex items-center gap-2 whitespace-nowrap">
          <UserPlus size={16} />
          Agregar Vendedor
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

        {/* Barra de filtros */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 px-5 py-4 border-b border-slate-100">
          <div className="flex items-center gap-3 flex-wrap">
            <div className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 w-full sm:w-64">
              <Filter size={14} className="text-slate-400 shrink-0" />
              <input
                type="text"
                placeholder="Buscar vendedor..."
                value={search}
                onChange={e => setSearch(e.target.value)}
                className="bg-transparent text-sm text-slate-600 placeholder:text-slate-400 outline-none w-full"
              />
            </div>
            {/* Filtro estado */}
            <div className="flex gap-2">
              {(['all', 'Active', 'Inactive'] as const).map(s => (
                <button
                  key={s}
                  onClick={() => setStatusFilter(s)}
                  className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-colors ${
                    statusFilter === s
                      ? 'bg-[#13316b] text-white'
                      : 'bg-slate-100 text-slate-500 hover:bg-slate-200'
                  }`}
                >
                  {s === 'all' ? 'Todos' : s}
                </button>
              ))}
            </div>
          </div>
          <Button variant="primary" size="sm" className="flex items-center gap-2 whitespace-nowrap">
            <Download size={14} />
            Exportar
          </Button>
        </div>

        {/* Tabla de vendedores */}
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 text-slate-400 text-left text-xs uppercase tracking-wide">
                <th className="px-6 py-4 font-medium">Vendedor</th>
                <th className="px-6 py-4 font-medium">Estado</th>
                <th className="px-6 py-4 font-medium">Contactos asignados</th>
                <th className="px-6 py-4 font-medium">Mensajes enviados</th>
                <th className="px-6 py-4 font-medium">Tasa de respuesta</th>
                <th className="px-6 py-4 font-medium">Última actividad</th>
                <th className="px-6 py-4 font-medium">Acción</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-10 text-center text-slate-400">
                    No se encontraron vendedores
                  </td>
                </tr>
              ) : (
                filtered.map(sp => (
                  <tr key={sp.id} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
                          {sp.avatar}
                        </div>
                        <div>
                          <p className="font-medium text-slate-800">{sp.name}</p>
                          <p className="text-xs text-slate-400">{sp.email}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <StatusDot status={sp.status} />
                    </td>
                    <td className="px-6 py-4 text-slate-600 font-medium">
                      {sp.assignedContacts}
                    </td>
                    <td className="px-6 py-4 text-slate-600">
                      {sp.messagesSent.toLocaleString()}
                    </td>
                    <td className="px-6 py-4">
                      <ResponseBar rate={sp.responseRate} />
                    </td>
                    <td className="px-6 py-4 text-slate-500 text-xs">
                      {sp.lastActivity}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <button className="text-blue-500 hover:text-blue-700 transition-colors">
                          <Pencil size={16} />
                        </button>
                        <button
                          className="text-slate-400 hover:text-red-500 transition-colors"
                          onClick={() => handleDelete(sp.id)}
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Paginación */}
        <div className="flex items-center justify-between px-6 py-4 border-t border-slate-100">
          <p className="text-sm text-slate-500">
            Mostrando {filtered.length} de {salespersons.length} vendedores
          </p>
          <div className="flex items-center gap-1">
            <button
              onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
              disabled={currentPage === 1}
              className="w-8 h-8 flex items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100 disabled:opacity-30 transition-colors text-sm"
            >
              {'<'}
            </button>
            {[1, 2, 3].map(page => (
              <button
                key={page}
                onClick={() => setCurrentPage(page)}
                className={`w-8 h-8 flex items-center justify-center rounded-lg text-sm font-medium transition-colors ${
                  currentPage === page
                    ? 'bg-[#13316b] text-white'
                    : 'text-slate-500 hover:bg-slate-100'
                }`}
              >
                {page}
              </button>
            ))}
            <button
              onClick={() => setCurrentPage(p => p + 1)}
              className="w-8 h-8 flex items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100 transition-colors text-sm"
            >
              {'>'}
            </button>
          </div>
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