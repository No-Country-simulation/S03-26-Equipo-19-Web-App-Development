import { useState } from 'react';
import { Plus, FileText, Download, RefreshCw } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Modal } from '../../components/ui/Modal';
import { useMutation } from '@tanstack/react-query';
import { exportData, downloadBlob } from '../../services/use_cases/export-service';
import type { ExportEntity, ExportFormat } from '../../types/admin.types';

type ExportStatus = 'idle' | 'loading' | 'done' | 'error';

interface ExportJob {
  id: number;
  format: ExportFormat;
  entity: ExportEntity;
  date: string;
  status: ExportStatus;
}

const KpiCard = ({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) => (
  <div className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm flex items-start gap-4">
    <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center shrink-0">{icon}</div>
    <div>
      <p className="text-slate-500 text-xs font-medium mb-1">{label}</p>
      <p className="text-2xl font-bold text-[#13316b]">{value}</p>
    </div>
  </div>
);

const StatusBadge = ({ status }: { status: ExportStatus }) => {
  const map: Record<ExportStatus, { label: string; dot: string; text: string }> = {
    idle: { label: 'PENDIENTE', dot: 'bg-slate-400', text: 'text-slate-500' },
    loading: { label: 'EN PROCESO', dot: 'bg-yellow-400', text: 'text-yellow-500' },
    done: { label: 'COMPLETADA', dot: 'bg-green-500', text: 'text-green-600' },
    error: { label: 'FALLIDA', dot: 'bg-red-400', text: 'text-red-500' },
  };
  const { label, dot, text } = map[status];
  return (
    <span className={`flex items-center gap-1.5 text-xs font-semibold ${text}`}>
      <span className={`w-2 h-2 rounded-full ${dot}`} />
      {label}
    </span>
  );
};

// --- PÁGINA PRINCIPAL ---
export const ExportsManagement = () => {
  const [jobs, setJobs] = useState<ExportJob[]>([]);
  const [nextId, setNextId] = useState(1);
  const [form, setForm] = useState<{ format: ExportFormat; entity: ExportEntity }>({
    format: 'CSV',
    entity: 'CONTACTS',
  });

  const mutation = useMutation({
    mutationFn: ({ format, entity }: { format: ExportFormat; entity: ExportEntity }) =>
      exportData({ format, entity }),
    onMutate: ({ format, entity }) => {
      const id = nextId;
      setNextId(n => n + 1);
      setJobs(prev => [
        {
          id,
          format,
          entity,
          date: new Date().toLocaleString('es-AR'),
          status: 'loading',
        },
        ...prev,
      ]);
      return { id };
    },
    onSuccess: (blob, { format, entity }, context) => {
      const { id } = context as { id: number };
      setJobs(prev => prev.map(j => j.id === id ? { ...j, status: 'done' } : j));
      downloadBlob(blob, `export_${entity}_${Date.now()}.${format.toLowerCase()}`);
    },
    onError: (_err, _vars, context) => {
      const { id } = context as { id: number };
      setJobs(prev => prev.map(j => j.id === id ? { ...j, status: 'error' } : j));
    },
  });

  const handleCreate = () => {
    mutation.mutate({ format: form.format, entity: form.entity });
  };

  const csvCount = jobs.filter(j => j.format === 'CSV').length;
  const pdfCount = jobs.filter(j => j.format === 'PDF').length;
  const doneCount = jobs.filter(j => j.status === 'done').length;

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Gestión de Exportaciones</h1>
          <p className="text-slate-500 text-sm mt-0.5 max-w-sm">
            Organiza y controla tus datos. Guarda registros de relaciones en formatos descargables.
          </p>
        </div>

        {/* Form inline para crear exportación */}
        <div className="flex items-center gap-2 flex-wrap">
          <select
            value={form.format}
            onChange={e => setForm(f => ({ ...f, format: e.target.value as ExportFormat }))}
            className="bg-white border border-slate-200 rounded-xl px-3 py-2 text-sm text-slate-700 outline-none focus:border-blue-400"
          >
            <option value="CSV">CSV</option>
            <option value="PDF">PDF</option>
          </select>
          <select
            value={form.entity}
            onChange={e => setForm(f => ({ ...f, entity: e.target.value as ExportEntity }))}
            className="bg-white border border-slate-200 rounded-xl px-3 py-2 text-sm text-slate-700 outline-none focus:border-blue-400"
          >
            <option value="contacts">Contactos</option>
            <option value="users">Usuarios</option>
            <option value="tasks">Tareas</option>
            <option value="funnel_stages">Etapas del embudo</option>
            <option value="salespersons">Vendedores</option>
            <option value="conversations">Conversaciones</option>
          </select>
          <Button
            variant="primary" size="md"
            className="flex items-center gap-2 whitespace-nowrap"
            onClick={handleCreate}
            disabled={mutation.isPending}
          >
            <Plus size={16} /> Crear Exportación
          </Button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <KpiCard icon={<FileText size={20} className="text-blue-600" />} label="Total de exportaciones" value={String(jobs.length)} />
        <KpiCard icon={<FileText size={20} className="text-blue-600" />} label="Exportaciones CSV" value={String(csvCount)} />
        <KpiCard icon={<FileText size={20} className="text-blue-600" />} label="Exportaciones PDF" value={String(pdfCount)} />
        <KpiCard icon={<FileText size={20} className="text-blue-600" />} label="Completadas"
          value={jobs.length > 0 ? `${Math.round((doneCount / jobs.length) * 100)}%` : '—'} />
      </div>

      {/* Tabla */}
      <div className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 text-slate-400 text-left text-xs uppercase tracking-wide">
                <th className="px-6 py-4 font-medium">Tipo</th>
                <th className="px-6 py-4 font-medium">Entidad</th>
                <th className="px-6 py-4 font-medium">Fecha</th>
                <th className="px-6 py-4 font-medium">Estado</th>
                <th className="px-6 py-4 font-medium">Acción</th>
              </tr>
            </thead>
            <tbody>
              {jobs.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-10 text-center text-slate-400">
                    No hay exportaciones. Creá una arriba.
                  </td>
                </tr>
              ) : (
                jobs.map(job => (
                  <tr key={job.id} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <div className="w-8 h-8 bg-blue-50 rounded-lg flex items-center justify-center">
                          <FileText size={15} className="text-blue-600" />
                        </div>
                        <span className="text-slate-700 font-medium">{job.format}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-slate-600 font-medium capitalize">{job.entity}</td>
                    <td className="px-6 py-4 text-slate-500">{job.date}</td>
                    <td className="px-6 py-4"><StatusBadge status={job.status} /></td>
                    <td className="px-6 py-4">
                      {job.status === 'loading' && (
                        <div className="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center text-slate-400">
                          <RefreshCw size={15} className="animate-spin" />
                        </div>
                      )}
                      {job.status === 'error' && (
                        <button
                          className="w-8 h-8 rounded-lg bg-red-50 flex items-center justify-center text-red-400 hover:bg-red-100 transition-colors"
                          onClick={() => mutation.mutate({ format: job.format, entity: job.entity })}
                        >
                          <RefreshCw size={15} />
                        </button>
                      )}
                      {job.status === 'done' && (
                        <button
                          className="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center text-blue-600 hover:bg-blue-100 transition-colors"
                          onClick={() => mutation.mutate({ format: job.format, entity: job.entity })}
                        >
                          <Download size={15} />
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

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