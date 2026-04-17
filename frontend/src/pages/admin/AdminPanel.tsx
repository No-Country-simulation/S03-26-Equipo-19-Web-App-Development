// src/pages/admin/AdminPanel.tsx
import { Download, Plus, Users, Mail, Calendar, Settings, UserPlus, Tag, AlertTriangle, ArrowUpRight } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Modal } from '../../components/ui/Modal';
import { useAuthStore } from '../../store/useAuthStore';
import { useGetPanelAdminMetrics } from '../../services/use_queries/metrics-query';
import { KpiCard } from '../../components/ui/KpiCard';
import { useState } from 'react';
import { AdminPanelForm } from '../../components/admin_panel/AdminPanelForm';
import { InboxTable } from '../../components/dashboard/InboxTable';
import { ExportForm } from '../../components/exports_management/ExportManagementForm';
import { useMutation } from '@tanstack/react-query';
import { exportData, downloadBlob } from '../../services/use_cases/export-service';
import type { ExportEntity, ExportFormat } from '../../types/admin.types';

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
  const [modalOpen, setModalOpen] = useState(false);
  const [exportModalOpen, setExportModalOpen] = useState(false);
  const { data: metricsPanel } = useGetPanelAdminMetrics();

  // Mutación para exportar datos
  const exportMutation = useMutation({
    mutationFn: ({ format, entity }: { format: ExportFormat; entity: ExportEntity }) =>
      exportData({ format, entity }),
    onSuccess: (blob, { format, entity }) => {
      downloadBlob(blob, `export_${entity}_${Date.now()}.${format.toLowerCase()}`);
      setExportModalOpen(false);
    },
    onError: (error) => {
      console.error('Error al exportar:', error);
      alert('Error al exportar los datos. Por favor, intenta nuevamente.');
    },
  });

  const handleExport = (format: ExportFormat, entity: ExportEntity) => {
    exportMutation.mutate({ format, entity });
  };

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
          {/* Botón Exportar Reporte con la lógica de exportación */}
          <Button 
            variant="outline" 
            size="sm" 
            className="border border-blue-600 text-blue-600 hover:bg-blue-50 flex items-center gap-2"
            onClick={() => setExportModalOpen(true)}
          >
            <Download size={15} /> Exportar Reporte
          </Button>
          <Button
            variant="primary"
            size="sm"
            className="flex items-center gap-2"
            onClick={() => setModalOpen(true)}
          >
            <Plus size={15} /> Nueva Campaña
          </Button>
        </div>
      </div>

      {/* KPIs */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <KpiCard
          title="Total de contactos"
          metric={metricsPanel?.totalContacts ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="primary"
          icon={<Users size={28} />}
        />
        <KpiCard
          title="Total de Mensajes"
          metric={metricsPanel?.totalMessages ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="secondary"
          icon={<Mail size={28} />}
        />
        <KpiCard
          title="Tareas pendientes"
          metric={metricsPanel?.upcomingTasks ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="secondary"
          icon={<Calendar size={28} />}
        />
        <KpiCard
          title="Tasa de respuesta"
          metric={metricsPanel?.responseRate ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="secondary"
          icon={<Settings size={28} />}
        />
      </div>

      {/* Contenido principal - InboxTable */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Columna izquierda (2/3) - InboxTable */}
        <div className="lg:col-span-2">
          <InboxTable maxItems={5} showViewAllButton={true} />
        </div>

        {/* Columna derecha (1/3) - Actividad Reciente */}
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
          <h2 className="text-base font-bold text-[#13316b] mb-5">Actividad Reciente</h2>
          {recentActivity.map(item => (
            <div key={item.id} className="flex gap-3 mb-4">
              <div className={`w-7 h-7 rounded-full flex items-center justify-center shrink-0 ${item.iconBg}`}>
                {item.icon}
              </div>
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

      {/* Sección de Rendimiento del Equipo */}
      <div className="mt-6">
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
          <h2 className="text-base font-bold text-[#13316b] mb-1">Rendimiento del Equipo</h2>
          <p className="text-xs text-slate-500 mb-4">Tasa de respuesta promedio por agente</p>
          <p className="text-sm text-slate-400">
            Visitá <span className="text-blue-600 font-medium">Métricas Globales</span> para ver el rendimiento detallado por agente.
          </p>
        </div>
      </div>
      
      {/* Modal para nueva campaña */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Nueva Campaña"
      >
        <AdminPanelForm
          onSubmit={() => setModalOpen(false)}
          onCancel={() => setModalOpen(false)}       
        />
      </Modal>

      {/* Modal para exportar reporte */}
      <Modal
        isOpen={exportModalOpen}
        onClose={() => setExportModalOpen(false)}
        title="Exportar Reporte"
      >
        <ExportForm
          onCancel={() => setExportModalOpen(false)}
          onSuccess={handleExport}
          isPending={exportMutation.isPending}
        />
      </Modal>
    </div>
  );
};