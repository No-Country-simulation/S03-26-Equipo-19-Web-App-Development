import { Users, MessageSquare, CheckSquare, Star, TrendingUp, TrendingDown, Minus, Filter } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend, BarChart, Bar } from 'recharts';

// --- TIPOS ---
interface KpiCardProps {
  icon: React.ReactNode;
  label: string;
  value: string;
  trend: string;
  isPositive: boolean;
}

interface AgentRow {
  id: number;
  name: string;
  avatar: string;
  status: 'active' | 'inactive';
  messagesSent: number;
  responseRate: number;
  trend: 'up' | 'down' | 'neutral';
}

// --- MOCK DATA ---
const kpiData: KpiCardProps[] = [
  {
    icon: <Users size={20} className="text-blue-600" />,
    label: 'Tasa de conversión',
    value: '285',
    trend: '+14.6%',
    isPositive: true,
  },
  {
    icon: <MessageSquare size={20} className="text-blue-600" />,
    label: 'Tasa de respuesta',
    value: '85%',
    trend: '+4.6%',
    isPositive: true,
  },
  {
    icon: <CheckSquare size={20} className="text-blue-600" />,
    label: 'Tareas completadas',
    value: '75%',
    trend: '+11.6%',
    isPositive: true,
  },
  {
    icon: <Star size={20} className="text-yellow-400" />,
    label: 'Mejor vendedor',
    value: '',
    trend: '',
    isPositive: true,
  },
];

const messagesOverTime = [
  { date: '1 Ene', salientes: 30, entrantes: 20 },
  { date: '8 Ene', salientes: 55, entrantes: 35 },
  { date: '15 Ene', salientes: 40, entrantes: 50 },
  { date: '22 Ene', salientes: 70, entrantes: 45 },
  { date: '29 Ene', salientes: 60, entrantes: 55 },
  { date: '5 Feb', salientes: 80, entrantes: 60 },
  { date: '12 Feb', salientes: 75, entrantes: 70 },
  { date: '19 Feb', salientes: 90, entrantes: 65 },
  { date: '26 Feb', salientes: 85, entrantes: 80 },
  { date: '5 Mar', salientes: 100, entrantes: 75 },
];

const contactGrowth = [
  { month: 'Nov', value: 300 },
  { month: 'Dec', value: 450 },
  { month: 'Jan', value: 380 },
  { month: 'Feb', value: 500 },
  { month: 'Mar', value: 420 },
];

const agentRows: AgentRow[] = [
  { id: 1, name: 'Sara Martínez', avatar: 'SM', status: 'active', messagesSent: 1402, responseRate: 88, trend: 'up' },
  { id: 2, name: 'Marcos Nodal', avatar: 'MN', status: 'active', messagesSent: 1190, responseRate: 80, trend: 'up' },
  { id: 3, name: 'Elena Rodríguez', avatar: 'ER', status: 'inactive', messagesSent: 954, responseRate: 35, trend: 'down' },
];

// --- SUBCOMPONENTES ---
const KpiCard = ({ icon, label, value, trend, isPositive }: KpiCardProps) => (
  <div className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm flex items-start gap-4">
    <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center shrink-0">
      {icon}
    </div>
    <div className="flex-1 min-w-0">
      <p className="text-slate-500 text-xs font-medium mb-1">{label}</p>
      {value ? (
        <>
          <p className="text-2xl font-bold text-[#13316b]">{value}</p>
          {trend && (
            <span className={`text-xs font-semibold ${isPositive ? 'text-green-500' : 'text-red-500'}`}>
              {trend}
            </span>
          )}
        </>
      ) : (
        <p className="text-base font-bold text-[#13316b]">Sara Martínez</p>
      )}
    </div>
  </div>
);

const TrendIcon = ({ trend }: { trend: 'up' | 'down' | 'neutral' }) => {
  if (trend === 'up') return <TrendingUp size={16} className="text-green-500" />;
  if (trend === 'down') return <TrendingDown size={16} className="text-red-400" />;
  return <Minus size={16} className="text-slate-400" />;
};

const StatusDot = ({ status }: { status: 'active' | 'inactive' }) => (
  <span className="flex items-center gap-1.5 text-xs">
    <span className={`w-2 h-2 rounded-full ${status === 'active' ? 'bg-green-500' : 'bg-red-400'}`} />
    <span className={status === 'active' ? 'text-green-600' : 'text-red-500'}>
      {status === 'active' ? 'Active' : 'Inactive'}
    </span>
  </span>
);

// --- PÁGINA PRINCIPAL ---
export const MetricsPage = () => {
  return (
    <div>
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Global Metrics</h1>
          <p className="text-slate-500 text-sm mt-0.5">
            Real-time engagement and relationship performance overview
          </p>
        </div>
        <div className="flex items-center gap-2 bg-white border border-slate-200 rounded-xl px-4 py-2 text-sm text-slate-600 shadow-sm">
          <Filter size={14} className="text-slate-400" />
          Últimos 30 días
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {kpiData.map((kpi, i) => (
          <KpiCard key={i} {...kpi} />
        ))}
      </div>

      {/* Gráficos */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">

        {/* Mensajes a lo largo del tiempo */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
          <h2 className="text-base font-bold text-[#13316b] mb-1">Mensajes a lo largo del tiempo</h2>
          <p className="text-xs text-slate-400 mb-4">Volumen de comunicación</p>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={messagesOverTime}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="date" tick={{ fontSize: 10, fill: '#94a3b8' }} />
              <YAxis tick={{ fontSize: 10, fill: '#94a3b8' }} />
              <Tooltip />
              <Legend
                formatter={(value: string) => (
                <span className="text-xs text-slate-500 capitalize">{value}</span>
                )}
              />
              <Line type="monotone" dataKey="salientes" stroke="#3b82f6" strokeWidth={2} dot={false} name="Salientes" />
              <Line type="monotone" dataKey="entrantes" stroke="#93c5fd" strokeWidth={2} dot={false} name="Entrantes" />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Crecimiento de contactos */}
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
          <h2 className="text-base font-bold text-[#13316b] mb-1">Crecimiento de contactos</h2>
          <p className="text-xs text-slate-400 mb-4">Leads vs. contactos nuevos</p>
          <ResponsiveContainer width="100%" height={140}>
            <BarChart data={contactGrowth}>
              <XAxis dataKey="month" tick={{ fontSize: 10, fill: '#94a3b8' }} />
              <Tooltip />
              <Bar dataKey="value" fill="#3b82f6" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
          <div className="flex justify-between items-end mt-4">
            <div>
              <p className="text-2xl font-bold text-[#13316b]">2,491</p>
              <p className="text-xs text-slate-400">Total de 2026</p>
            </div>
            <div className="text-right">
              <p className="text-xl font-bold text-blue-500">342</p>
              <p className="text-xs text-slate-400">Leads nuevos</p>
            </div>
          </div>
        </div>
      </div>

      {/* Rendimiento por agente */}
      <div className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b border-slate-100">
          <h2 className="text-base font-bold text-[#13316b]">Rendimiento por agente</h2>
          <p className="text-xs text-slate-400 mt-0.5">Métricas de eficiencia para el personal</p>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 text-slate-400 text-left text-xs uppercase tracking-wide">
                <th className="px-6 py-4 font-medium">Vendedor</th>
                <th className="px-6 py-4 font-medium">Estado</th>
                <th className="px-6 py-4 font-medium">Mensajes enviados</th>
                <th className="px-6 py-4 font-medium">Tasa de respuesta</th>
                <th className="px-6 py-4 font-medium">Tendencia</th>
              </tr>
            </thead>
            <tbody>
              {agentRows.map(agent => (
                <tr key={agent.id} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
                        {agent.avatar}
                      </div>
                      <span className="font-medium text-slate-700">{agent.name}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <StatusDot status={agent.status} />
                  </td>
                  <td className="px-6 py-4 text-slate-600">{agent.messagesSent.toLocaleString()}</td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <div className="w-24 bg-slate-100 rounded-full h-1.5">
                        <div
                          className="bg-[#3b82f6] h-1.5 rounded-full"
                          style={{ width: `${agent.responseRate}%` }}
                        />
                      </div>
                      <span className="text-slate-600 text-xs">{agent.responseRate}%</span>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <TrendIcon trend={agent.trend} />
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