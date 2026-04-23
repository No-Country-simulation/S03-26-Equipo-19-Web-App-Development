// src/pages/admin/MetricsPage.tsx
import { Users, MessageSquare, CheckSquare, TrendingUp, TrendingDown, Minus, Filter, Award } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend, BarChart, Bar } from 'recharts';
import { useGetGLobalMetrics, useGetMetricsByPeriod, useGetPanelAdminMetrics } from '../../services/use_queries/metrics-query';
import { useGetSalespersons } from '../../services/use_queries/salespersons-query';
import type { SalespersonResponse } from '../../types/admin.types';
import { KpiCard } from '../../components/ui/KpiCard';
import { KpiCardTopSaler } from '../../components/ui/KpiCardTopSaler';
import type { Metric } from '../../types/metric.types';

// --- SUBCOMPONENTES ---

const TrendIcon = ({ trend }: { trend: 'up' | 'down' | 'neutral' }) => {
  if (trend === 'up') return <TrendingUp size={16} className="text-green-500" />;
  if (trend === 'down') return <TrendingDown size={16} className="text-red-400" />;
  return <Minus size={16} className="text-slate-400" />;
};

const StatusDot = ({ status }: { status: string }) => (
  <span className="flex items-center gap-1.5 text-xs">
    <span className={`w-2 h-2 rounded-full ${status === 'ACTIVE' ? 'bg-green-500' : 'bg-red-400'}`} />
    <span className={status === 'ACTIVE' ? 'text-green-600' : 'text-red-500'}>
      {status === 'ACTIVE' ? 'Active' : 'Inactive'}
    </span>
  </span>
);

// --- PÁGINA PRINCIPAL ---
export const MetricsPage = () => {
  const { data: globalMetrics } = useGetGLobalMetrics();
  const { data: panelMetrics } = useGetPanelAdminMetrics();
  const { data: periodData = [] } = useGetMetricsByPeriod('2026-01-01', '2026-03-31');
  const { data: salespersons = [] } = useGetSalespersons();

  // Adaptar period data al formato del gráfico
  const chartData = periodData.map((p) => ({
    date: p.date,
    salientes: p.outbound,
    entrantes: p.inbound,
  }));

  // Construir métricas para las KPI cards
  const totalConversationsMetric: Metric = globalMetrics?.totalConversations ?? {
    value: 0,
    changePercent: 0,
    trend: 'stable'
  };

  const responseRateMetric: Metric = globalMetrics?.responseRate ?? {
    value: 0,
    changePercent: 0,
    trend: 'stable'
  };

  const completedTasksMetric: Metric = globalMetrics?.completedTasks ?? {
    value: 0,
    changePercent: 0,
    trend: 'stable'
  };

  return (
    <div>
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Global Metrics</h1>
          <p className="text-slate-500 text-sm mt-0.5">Real-time engagement and relationship performance overview</p>
        </div>
        <div className="flex items-center gap-2 bg-white border border-slate-200 rounded-xl px-4 py-2 text-sm text-slate-600 shadow-sm">
          <Filter size={14} className="text-slate-400" />
          Últimos 30 días
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {/* Card 1: Total Conversaciones */}
        <KpiCard
          title="Total Conversaciones"
          metric={totalConversationsMetric}
          color="primary"
          icon={<Users size={28} />}
          impact="positive"
        />

        {/* Card 2: Tasa de respuesta */}
        <KpiCard
          title="Tasa de respuesta"
          metric={responseRateMetric}
          color="secondary"
          icon={<MessageSquare size={28} />}
          impact="positive"
        />

        {/* Card 3: Tareas completadas */}
        <KpiCard
          title="Tareas completadas"
          metric={completedTasksMetric}
          color="secondary"
          icon={<CheckSquare size={28} />}
          impact="positive"
        />

        {/* Card 4: Mejor Vendedor */}
        {globalMetrics?.topSalesperson ? (
          <KpiCardTopSaler person={globalMetrics.topSalesperson} />
        ) : (
          <KpiCard
            title="Mejor Vendedor"
            metric={{ value: 0, changePercent: 0, trend: 'stable' }}
            color="secondary"
            icon={<Award size={28} />}
            impact="positive"
          />
        )}
      </div>

      {/* Gráficos */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
          <h2 className="text-base font-bold text-[#13316b] mb-1">Mensajes a lo largo del tiempo</h2>
          <p className="text-xs text-slate-400 mb-4">Volumen de comunicación</p>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={chartData.length > 0 ? chartData : []}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="date" tick={{ fontSize: 10, fill: '#94a3b8' }} />
              <YAxis tick={{ fontSize: 10, fill: '#94a3b8' }} />
              <Tooltip />
              <Legend formatter={(value: string) => <span className="text-xs text-slate-500 capitalize">{value}</span>} />
              <Line type="monotone" dataKey="salientes" stroke="#3b82f6" strokeWidth={2} dot={false} name="Salientes" />
              <Line type="monotone" dataKey="entrantes" stroke="#93c5fd" strokeWidth={2} dot={false} name="Entrantes" />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
          <h2 className="text-base font-bold text-[#13316b] mb-1">Crecimiento de contactos</h2>
          <p className="text-xs text-slate-400 mb-4">Leads vs. contactos nuevos</p>
          <ResponsiveContainer width="100%" height={140}>
            <BarChart data={chartData}>
              <XAxis dataKey="date" tick={{ fontSize: 10, fill: '#94a3b8' }} />
              <Tooltip />
              <Bar dataKey="entrantes" fill="#3b82f6" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
          <div className="flex justify-between items-end mt-4">
            <div>
              <p className="text-2xl font-bold text-[#13316b]">{panelMetrics?.totalContacts.value ?? '—'}</p>
              <p className="text-xs text-slate-400">Total contactos</p>
            </div>
            <div className="text-right">
              <p className="text-xl font-bold text-blue-500">{panelMetrics?.totalMessages.value ?? '—'}</p>
              <p className="text-xs text-slate-400">Total mensajes</p>
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
              {salespersons.length === 0 ? (
                <tr><td colSpan={5} className="px-6 py-10 text-center text-slate-400">Sin datos de agentes</td></tr>
              ) : (
                salespersons.map((agent: SalespersonResponse) => {
                  const initials = agent.name.split(' ').map((n: string) => n[0]).join('').slice(0, 2).toUpperCase();
                  const trend: 'up' | 'down' | 'neutral' =
                    agent.responseRate >= 70 ? 'up' : agent.responseRate >= 50 ? 'neutral' : 'down';
                  return (
                    <tr key={agent.id} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-9 h-9 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
                            {initials}
                          </div>
                          <span className="font-medium text-slate-700">{agent.name}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4"><StatusDot status={agent.status} /></td>
                      <td className="px-6 py-4 text-slate-600">{(agent.messagesSent ?? 0).toLocaleString()}</td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2">
                          <div className="w-24 bg-slate-100 rounded-full h-1.5">
                            <div className="bg-[#3b82f6] h-1.5 rounded-full" style={{ width: `${agent.responseRate ?? 0}%` }} />
                          </div>
                          <span className="text-slate-600 text-xs">{agent.responseRate ?? 0}%</span>
                        </div>
                      </td>
                      <td className="px-6 py-4"><TrendIcon trend={trend} /></td>
                    </tr>
                  );
                })
              )}
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