import { Download, Plus, Users, Mail, Calendar, Settings, UserPlus, Tag, AlertTriangle, ArrowUpRight } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { useGetConversations } from '../../services/use_queries/conversations-query';
import { useAuthStore } from '../../store/useAuthStore';
import type { ConversationResType } from '../../types/conversation.types';
import { useGetPanelAdminMetrics } from '../../services/use_queries/metrics-query';
import { KpiCard } from '../../components/ui/KpiCard';



const InboxRow = ({ conv }: { conv: ConversationResType }) => {
  const contactName = conv.contact?.name ?? '?';
  const initials = contactName.split(' ').map((n: string) => n[0]).join('').slice(0, 2).toUpperCase();
  return (
    <tr className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
      <td className="px-4 py-3">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
            {initials}
          </div>
          <span className="text-slate-600 text-sm truncate max-w-[200px]">{contactName}</span>
        </div>
      </td>
      <td className="px-4 py-3 text-slate-500 text-sm">{conv.contact?.email ?? '—'}</td>
      <td className="px-4 py-3 text-slate-500 text-sm">
        {conv.lastInteraction ? new Date(conv.lastInteraction).toLocaleDateString('es-AR') : '—'}
      </td>
      <td className="px-4 py-3 text-slate-500 text-sm capitalize">{conv.channel ?? '—'}</td>
      <td className="px-4 py-3">
        <div className="w-7 h-7 rounded-full bg-slate-100 flex items-center justify-center">
          <Users size={13} className="text-slate-500" />
        </div>
      </td>
    </tr>
  );
};

const recentActivity = [
  { id: 1, icon: <UserPlus size={14} />, iconBg: 'bg-blue-100 text-blue-600', title: 'Panel activo', description: 'Datos cargados desde la API.', time: 'ahora' },
  { id: 2, icon: <ArrowUpRight size={14} />, iconBg: 'bg-green-100 text-green-600', title: 'Métricas actualizadas', description: 'KPIs sincronizados con el backend.', time: 'ahora' },
  { id: 3, icon: <Download size={14} />, iconBg: 'bg-purple-100 text-purple-600', title: 'Sistema operativo', description: 'Todos los servicios responden correctamente.', time: 'ahora' },
  { id: 4, icon: <Tag size={14} />, iconBg: 'bg-yellow-100 text-yellow-600', title: 'Conversaciones cargadas', description: 'Bandeja de entrada sincronizada.', time: 'ahora' },
  { id: 5, icon: <AlertTriangle size={14} />, iconBg: 'bg-red-100 text-red-600', title: 'Revisá tareas vencidas', description: 'Hay tareas pendientes sin completar.', time: 'pendiente' },
];

// --- PÁGINA PRINCIPAL ---
export const AdminPanel = () => {
  const user = useAuthStore(s => s.user);
  const { data: metricsPanel } = useGetPanelAdminMetrics();
  const { data: conversations = [] } = useGetConversations();

   const inboxPreview = (conversations as ConversationResType[]).slice(0, 5);

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Panel</h1>
          <p className="text-slate-500 text-sm mt-0.5">
            Bienvenido de nuevo, {user?.name ?? 'Admin'}. Esto es lo que está sucediendo hoy.
          </p>
        </div>
        <div className="flex gap-3">
          <Button variant="outline" size="sm" className="border border-blue-600 text-blue-600 hover:bg-blue-50 flex items-center gap-2">
            <Download size={15} /> Exportar Reporte
          </Button>
          <Button variant="primary" size="sm" className="flex items-center gap-2">
            <Plus size={15} /> Nueva Campaña
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <KpiCard
          title="Total de contactos"
          metric={metricsPanel?.totalContacts ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="primary"
          icon={<Users size={28} />}
        />
        <KpiCard
          title="Total de contactos"
          metric={metricsPanel?.totalMessages ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="secondary"
          icon={<Mail size={28} />}
        />
        <KpiCard
          title="Tareas pendientes"
          metric={metricsPanel?.uncomingTasks ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="secondary"
          icon={<Calendar size={28} />}
        />
        <KpiCard
          title="Tasa de respuesta"
          metric={metricsPanel?.uncomingTasks ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="secondary"
          icon={<Settings size={28} />}
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 flex flex-col gap-6">

          <div className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
            <div className="flex justify-between items-center px-5 py-4 border-b border-slate-100">
              <h2 className="text-base font-bold text-[#13316b]">Bandeja de entrada</h2>
              <button className="text-slate-400 hover:text-slate-600 transition-colors"><Settings size={18} /></button>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full">
                <tbody>
                  {inboxPreview.length === 0 ? (
                    <tr><td colSpan={5} className="px-4 py-8 text-center text-slate-400 text-sm">No hay conversaciones recientes</td></tr>
                  ) : (
                    inboxPreview.map((conv: ConversationResType) => <InboxRow key={conv.id} conv={conv} />)
                  )}
                </tbody>
              </table>
            </div>
            <div className="px-5 py-3 border-t border-slate-100 flex justify-end">
              <button className="text-blue-600 text-sm font-semibold hover:underline">VER TODO</button>
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
            <h2 className="text-base font-bold text-[#13316b] mb-1">Rendimiento del Equipo</h2>
            <p className="text-xs text-slate-500 mb-4">Tasa de respuesta promedio por agente</p>
            <p className="text-sm text-slate-400">
              Visitá <span className="text-blue-600 font-medium">Métricas Globales</span> para ver el rendimiento detallado por agente.
            </p>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
          <h2 className="text-base font-bold text-[#13316b] mb-5">Actividad Reciente</h2>
          {recentActivity.map(item => (
            <div key={item.id} className="flex gap-3 mb-4">
              <div className={`w-7 h-7 rounded-full flex items-center justify-center shrink-0 ${item.iconBg}`}>{item.icon}</div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-slate-700">{item.title}</p>
                <p className="text-xs text-slate-500 mt-0.5">{item.description}</p>
                <p className="text-xs text-slate-400 mt-1">{item.time}</p>
              </div>
            </div>
          ))}
          <button className="w-full mt-2 py-2 border border-slate-200 rounded-xl text-sm text-slate-600 hover:bg-slate-50 transition-colors font-medium">
            Ver registro de auditoría
          </button>
        </div>
      </div>
    </div>
  );
};