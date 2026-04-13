import { useState } from 'react';
import { Plus, FileText, CheckCircle } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { useQuery } from '@tanstack/react-query';
import { apiContactsService } from '../../services/general_api';
import type { Task } from '../../types/task.types';

const KpiCard = ({ icon, label, value, trend, isPositive }: {
  icon: React.ReactNode; label: string; value: string; trend: string; isPositive: boolean;
}) => (
  <div className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm flex items-start gap-4">
    <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center shrink-0">{icon}</div>
    <div>
      <p className="text-slate-500 text-xs font-medium mb-1">{label}</p>
      <p className="text-2xl font-bold text-[#13316b]">{value}</p>
      <span className={`text-xs font-semibold ${isPositive ? 'text-green-500' : 'text-red-500'}`}>{trend}</span>
    </div>
  </div>
);

const StatusBadge = ({ type }: { type: string }) => {
  const styles: Record<string, string> = {
    'follow-up': 'bg-blue-50 text-blue-600',
    'reminder':  'bg-yellow-50 text-yellow-600',
    'pending':   'bg-orange-50 text-orange-500',
    'done':      'bg-green-50 text-green-600',
  };
  const labels: Record<string, string> = {
    'follow-up': 'Seguimiento',
    'reminder':  'Recordatorio',
    'pending':   'Pendiente',
    'done':      'Completada',
  };
  return (
    <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${styles[type] ?? 'bg-slate-100 text-slate-500'}`}>
      {labels[type] ?? type}
    </span>
  );
};

const isTaskOverdue = (task: Task): boolean => {
  if (!task.dueDate || task.status === 'done') return false;
  return new Date(task.dueDate) < new Date();
};

export const TasksManagement = () => {
  const [currentPage, setCurrentPage] = useState(1);

  const { data: tasks = [], isLoading, isError } = useQuery<Task[]>({
    queryKey: ['admin-tasks'],
    queryFn: async () => {
      const res = await apiContactsService.get('/tasks');
      return res.data;
    },
  });

  const pending  = tasks.filter((t) => t.status === 'pending');
  const done     = tasks.filter((t) => t.status === 'done');
  const overdue  = tasks.filter((t) => isTaskOverdue(t));
  const compliance = tasks.length > 0 ? Math.round((done.length / tasks.length) * 100) : 0;

  const kpiData = [
    { icon: <FileText size={20} className="text-blue-600" />, label: 'Tareas pendientes',  value: String(pending.length), trend: '', isPositive: false },
    { icon: <FileText size={20} className="text-blue-600" />, label: 'Tareas completadas', value: String(done.length),    trend: '', isPositive: true  },
    { icon: <FileText size={20} className="text-red-500"  />, label: 'Tareas vencidas',    value: String(overdue.length), trend: '', isPositive: false },
    { icon: <FileText size={20} className="text-blue-600" />, label: 'Cumplimiento',        value: `${compliance}%`,       trend: '', isPositive: true  },
  ];

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Gestión de tareas</h1>
          <p className="text-slate-500 text-sm mt-0.5">Organiza y da seguimiento a las tareas del equipo</p>
        </div>
        <Button variant="primary" size="md" className="flex items-center gap-2 whitespace-nowrap">
          <Plus size={16} /> Crear Tarea
        </Button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {kpiData.map((kpi, i) => <KpiCard key={i} {...kpi} />)}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* Tareas pendientes */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
          <div className="px-5 py-4 border-b border-slate-100">
            <h2 className="text-base font-bold text-[#13316b]">Tareas pendientes</h2>
          </div>

          <div className="divide-y divide-slate-50">
            {isLoading ? (
              <div className="px-5 py-10 text-center text-slate-400 text-sm">Cargando tareas...</div>
            ) : isError ? (
              <div className="px-5 py-10 text-center text-red-400 text-sm">Error al cargar tareas</div>
            ) : pending.length === 0 ? (
              <div className="px-5 py-10 text-center text-slate-400 text-sm">No hay tareas pendientes</div>
            ) : (
              pending.map((task) => (
                <div key={task.id} className="px-5 py-4 hover:bg-slate-50 transition-colors">
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex items-start gap-3 flex-1 min-w-0">
                      <input type="checkbox" className="mt-1 w-4 h-4 rounded border-slate-300 text-blue-600 shrink-0 cursor-pointer" />
                      <div className="flex-1 min-w-0">
                        <p className={`text-sm font-medium ${isTaskOverdue(task) ? 'text-slate-500' : 'text-slate-700'}`}>
                          {task.title}
                        </p>
                        <p className="text-xs text-slate-400 mt-1">
                          Vence:{' '}
                          {isTaskOverdue(task) ? (
                            <span className="text-red-500 font-semibold">
                              Vencido ({new Date(task.dueDate).toLocaleDateString('es-AR')})
                            </span>
                          ) : (
                            new Date(task.dueDate).toLocaleDateString('es-AR')
                          )}
                        </p>
                      </div>
                    </div>
                    <StatusBadge type={task.type} />
                  </div>
                </div>
              ))
            )}
          </div>

          <div className="flex items-center justify-between px-5 py-4 border-t border-slate-100">
            <p className="text-sm text-slate-500">Mostrando {pending.length} tareas pendientes</p>
            <div className="flex items-center gap-1">
              <button onClick={() => setCurrentPage(p => Math.max(1, p - 1))} disabled={currentPage === 1}
                className="w-8 h-8 flex items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100 disabled:opacity-30 transition-colors text-sm">{'<'}</button>
              {[1, 2, 3].map(page => (
                <button key={page} onClick={() => setCurrentPage(page)}
                  className={`w-8 h-8 flex items-center justify-center rounded-lg text-sm font-medium transition-colors ${
                    currentPage === page ? 'bg-[#13316b] text-white' : 'text-slate-500 hover:bg-slate-100'
                  }`}>{page}</button>
              ))}
              <button onClick={() => setCurrentPage(p => p + 1)}
                className="w-8 h-8 flex items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100 transition-colors text-sm">{'>'}</button>
            </div>
          </div>
        </div>

        {/* Recientemente completado */}
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
          <div className="flex justify-between items-center mb-5">
            <h2 className="text-base font-bold text-[#13316b]">Recientemente completado</h2>
            <button className="text-blue-600 text-xs font-semibold hover:underline">VER TODO</button>
          </div>
          <div className="flex flex-col gap-4">
            {done.slice(0, 6).map((task) => (
              <div key={task.id} className="flex items-start gap-3">
                <div className="w-7 h-7 rounded-full bg-green-100 flex items-center justify-center shrink-0">
                  <CheckCircle size={14} className="text-green-500" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-700">{task.title}</p>
                  <p className="text-xs text-slate-400 mt-0.5">
                    {task.dueDate
                      ? new Date(task.dueDate).toLocaleDateString('es-AR', { day: 'numeric', month: 'short' })
                      : 'Completado'}
                  </p>
                </div>
              </div>
            ))}
            {done.length === 0 && !isLoading && (
              <p className="text-sm text-slate-400">No hay tareas completadas aún</p>
            )}
          </div>
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