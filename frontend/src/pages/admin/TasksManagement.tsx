import { useState } from 'react';
import { Plus, FileText, CheckCircle } from 'lucide-react';
import { Button } from '../../components/ui/Button';

// --- TIPOS ---
type TaskStatus = 'Seguimiento' | 'Recordatorio' | 'Vencida';

interface Task {
  id: number;
  title: string;
  assignedTo: string;
  responsible: string;
  dueDate: string;
  status: TaskStatus;
  isOverdue?: boolean;
}

interface RecentTask {
  id: number;
  title: string;
  completedAt: string;
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
    label: 'Tareas pendientes',
    value: '64',
    trend: '+11.6%',
    isPositive: false,
  },
  {
    icon: <FileText size={20} className="text-blue-600" />,
    label: 'Tareas completadas',
    value: '178',
    trend: '+4.5%',
    isPositive: true,
  },
  {
    icon: <FileText size={20} className="text-red-500" />,
    label: 'Tareas vencidas',
    value: '30',
    trend: '+8.6%',
    isPositive: false,
  },
  {
    icon: <FileText size={20} className="text-blue-600" />,
    label: 'Cumplimiento',
    value: '40%',
    trend: '+7.06%',
    isPositive: true,
  },
];

const mockTasks: Task[] = [
  {
    id: 1,
    title: 'Llamar a Juan por propuesta Confirmar interés y enviar cotización',
    assignedTo: 'Ana García',
    responsible: 'Juan Pérez',
    dueDate: '05/04/2026',
    status: 'Seguimiento',
  },
  {
    id: 2,
    title: 'Enviar cotización y hacer seguimiento por WhatsApp',
    assignedTo: 'Carlos Gómez',
    responsible: 'Carlos Gómez',
    dueDate: '20/04/2026',
    status: 'Seguimiento',
  },
  {
    id: 3,
    title: 'Revisar bandeja de leads',
    assignedTo: 'Rodrigo Pérez',
    responsible: 'Rodrigo Pérez',
    dueDate: '20/04/2026',
    status: 'Recordatorio',
  },
  {
    id: 4,
    title: 'Llamar para confirmar interés en el servicio y próximos pasos',
    assignedTo: 'Mario Sosa',
    responsible: 'Luis Vargas',
    dueDate: 'hoy',
    status: 'Vencida',
    isOverdue: true,
  },
];

const recentTasks: RecentTask[] = [
  { id: 1, title: 'Llamar a cliente interesado', completedAt: 'Completado hace 2 horas' },
  { id: 2, title: 'Enviar propuesta comercial', completedAt: 'Completado hoy, 9:23 a.m.' },
  { id: 3, title: 'Responder consulta por WhatsApp', completedAt: 'Completado hoy' },
  { id: 4, title: 'Actualizar estado del contacto', completedAt: 'Completado hoy' },
  { id: 5, title: 'Revisar nuevos leads', completedAt: 'Completado ayer' },
  { id: 6, title: 'Actualizar estado del contacto', completedAt: 'Completado ayer' },
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

const StatusBadge = ({ status }: { status: TaskStatus }) => {
  const styles: Record<TaskStatus, string> = {
    'Seguimiento': 'bg-blue-50 text-blue-600',
    'Recordatorio': 'bg-yellow-50 text-yellow-600',
    'Vencida': 'bg-red-50 text-red-500',
  };
  return (
    <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${styles[status]}`}>
      {status}
    </span>
  );
};

// --- PÁGINA PRINCIPAL ---
export const TasksManagement = () => {
  const [currentPage, setCurrentPage] = useState(1);
  const totalTasks = 32;

  return (
    <div>
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Gestión de tareas</h1>
          <p className="text-slate-500 text-sm mt-0.5">Organiza y da seguimiento a las tareas del equipo</p>
        </div>
        <Button variant="primary" size="md" className="flex items-center gap-2 whitespace-nowrap">
          <Plus size={16} />
          Create Task
        </Button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {kpiData.map((kpi, i) => (
          <KpiCard key={i} {...kpi} />
        ))}
      </div>

      {/* Contenido principal */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* Tareas pendientes */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
          <div className="px-5 py-4 border-b border-slate-100">
            <h2 className="text-base font-bold text-[#13316b]">Tareas pendientes</h2>
          </div>

          <div className="divide-y divide-slate-50">
            {mockTasks.map(task => (
              <div key={task.id} className="px-5 py-4 hover:bg-slate-50 transition-colors">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-start gap-3 flex-1 min-w-0">
                    <input
                      type="checkbox"
                      className="mt-1 w-4 h-4 rounded border-slate-300 text-blue-600 shrink-0 cursor-pointer"
                    />
                    <div className="flex-1 min-w-0">
                      <p className={`text-sm font-medium ${task.isOverdue ? 'text-slate-500' : 'text-slate-700'}`}>
                        {task.title}
                      </p>
                      <p className="text-xs text-slate-400 mt-1">
                        A: Asignado a: {task.assignedTo} &nbsp;·&nbsp;
                        <span className="font-medium text-slate-500">{task.responsible}</span>
                        &nbsp;·&nbsp; Vence:{' '}
                        {task.isOverdue ? (
                          <span className="text-red-500 font-semibold">Vencido ({task.dueDate})</span>
                        ) : (
                          task.dueDate
                        )}
                      </p>
                    </div>
                  </div>
                  <StatusBadge status={task.status} />
                </div>
              </div>
            ))}
          </div>

          {/* Paginación */}
          <div className="flex items-center justify-between px-5 py-4 border-t border-slate-100">
            <p className="text-sm text-slate-500">Mostrando 1-4 de {totalTasks} tareas</p>
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

        {/* Recientemente completado */}
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
          <div className="flex justify-between items-center mb-5">
            <h2 className="text-base font-bold text-[#13316b]">Recientemente completado</h2>
            <button className="text-blue-600 text-xs font-semibold hover:underline">VER TODO</button>
          </div>
          <div className="flex flex-col gap-4">
            {recentTasks.map(task => (
              <div key={task.id} className="flex items-start gap-3">
                <div className="w-7 h-7 rounded-full bg-green-100 flex items-center justify-center shrink-0">
                  <CheckCircle size={14} className="text-green-500" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-700">{task.title}</p>
                  <p className="text-xs text-slate-400 mt-0.5">{task.completedAt}</p>
                </div>
              </div>
            ))}
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