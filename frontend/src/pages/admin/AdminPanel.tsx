import { Download, Users, Mail, Calendar, MessageCircle, Settings } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { useGetConversationsInbox } from '../../services/use_queries/conversations-query';
import { useAuthStore } from '../../store/useAuthStore';
import type { ConversationInboxItemType } from '../../types/conversation.types';
import { useGetPanelAdminMetrics } from '../../services/use_queries/metrics-query';
import { KpiCard } from '../../components/ui/KpiCard';
import { useMutation } from '@tanstack/react-query';
import { exportData, downloadBlob } from '../../services/use_cases/export-service';
import type { ExportFormat, ExportEntity } from '../../types/admin.types';

// ─── FILA DE BANDEJA DE ENTRADA ─────────────────────────────────────────────
const InboxRow = ({ item }: { item: ConversationInboxItemType }) => {
  const initials = item.contactName
    .split(' ')
    .map((n) => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();
  return (
    <tr className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
      <td className="px-4 py-3">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
            {initials}
          </div>
          <span className="text-slate-600 text-sm truncate max-w-[200px]">{item.contactName}</span>
        </div>
      </td>
      <td className="px-4 py-3 text-slate-500 text-sm">{item.contactIdentifier}</td>
      <td className="px-4 py-3 text-slate-500 text-sm">
        {item.lastMessageAt ? new Date(item.lastMessageAt).toLocaleDateString('es-AR') : '—'}
      </td>
      <td className="px-4 py-3 text-slate-500 text-sm capitalize">{item.channel}</td>
      <td className="px-4 py-3">
        <div className="w-7 h-7 rounded-full bg-slate-100 flex items-center justify-center">
          <MessageCircle size={13} className="text-slate-500" />
        </div>
      </td>
    </tr>
  );
};

// ─── PÁGINA PRINCIPAL ────────────────────────────────────────────────────────
export const AdminPanel = () => {
  const user = useAuthStore((s) => s.user);

  const { data: metricsPanel } = useGetPanelAdminMetrics();
  const { data: inboxItems = [] } = useGetConversationsInbox();

  const inboxPreview = inboxItems.slice(0, 5);

  // Mutación para exportar reporte
  const exportMutation = useMutation({
    mutationFn: ({ format, entityType }: { format: ExportFormat; entityType: ExportEntity }) =>
      exportData({ format, entityType }),
    onSuccess: (blob, { format }) => {
      const timestamp = new Date().toISOString().slice(0, 10);
      downloadBlob(blob, `reporte_panel_${timestamp}.${format.toLowerCase()}`);
    },
    onError: (error: unknown) => {
      if (
        error &&
        typeof error === 'object' &&
        'response' in error &&
        error.response &&
        typeof error.response === 'object' &&
        'data' in error.response &&
        error.response.data instanceof Blob
      ) {
        error.response.data.text().then((text: string) => {
          console.error('Error del servidor:', text);
          alert('Error al exportar: ' + text);
        });
      } else {
        console.error('Error de exportación:', error);
        alert('No se pudo generar el reporte. Revisa la consola.');
      }
    },
  });

const handleExportReport = () => {
  exportMutation.mutate({ format: 'CSV', entityType: 'CONTACTS' });
};

  return (
    <div>
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Panel</h1>
          <p className="text-slate-500 text-sm mt-0.5">
            Bienvenido de nuevo, {user?.name ?? 'Admin'}. Esto es lo que está sucediendo hoy.
          </p>
        </div>
        <div className="flex gap-3">
          <Button
            variant="outline"
            size="sm"
            className="border border-blue-600 text-blue-600 hover:bg-blue-50 flex items-center gap-2"
            onClick={handleExportReport}
            loading={exportMutation.isPending}
          >
            <Download size={15} /> Exportar Reporte
          </Button>
        </div>
      </div>

      {/* KPIs - 3 columnas */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
        <KpiCard
          title="Total de contactos"
          metric={metricsPanel?.totalContacts ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="primary"
          icon={<Users size={28} />}
        />
        <KpiCard
          title="Mensajes enviados"
          metric={metricsPanel?.totalMessages ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="secondary"
          icon={<Mail size={28} />}
        />
        <KpiCard
          title="Tareas próximas"
          metric={metricsPanel?.uncomingTasks ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="secondary"
          icon={<Calendar size={28} />}
        />
      </div>

      {/* Contenido principal */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 flex flex-col gap-6">
          {/* Bandeja de entrada */}
          <div className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
            <div className="flex justify-between items-center px-5 py-4 border-b border-slate-100">
              <h2 className="text-base font-bold text-[#13316b]">Bandeja de entrada</h2>
              <button className="text-slate-400 hover:text-slate-600 transition-colors">
                <Settings size={18} />
              </button>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full">
                <tbody>
                  {inboxPreview.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-4 py-8 text-center text-slate-400 text-sm">
                        No hay conversaciones recientes
                      </td>
                    </tr>
                  ) : (
                    inboxPreview.map((item) => <InboxRow key={item.conversationId} item={item} />)
                  )}
                </tbody>
              </table>
            </div>
            <div className="px-5 py-3 border-t border-slate-100 flex justify-end">
              <button className="text-blue-600 text-sm font-semibold hover:underline">
                VER TODO
              </button>
            </div>
          </div>

          {/* Rendimiento del equipo */}
          <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
            <h2 className="text-base font-bold text-[#13316b] mb-1">Rendimiento del Equipo</h2>
            <p className="text-xs text-slate-500 mb-4">Tasa de respuesta promedio por agente</p>
            <p className="text-sm text-slate-400">
              Visitá <span className="text-blue-600 font-medium">Métricas Globales</span> para ver el
              rendimiento detallado por agente.
            </p>
          </div>
        </div>

        {/* Actividad Reciente (estática) */}
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
          <h2 className="text-base font-bold text-[#13316b] mb-5">Actividad Reciente</h2>
          <p className="text-sm text-slate-400">No hay actividad reciente para mostrar.</p>
          <button className="w-full mt-2 py-2 border border-slate-200 rounded-xl text-sm text-slate-600 hover:bg-slate-50 transition-colors font-medium">
            Ver registro de auditoría
          </button>
        </div>
      </div>
    </div>
  );
};