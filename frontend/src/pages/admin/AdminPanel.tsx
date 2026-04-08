import { Download, Plus, Users, Mail, Calendar, Settings, UserPlus, Tag, AlertTriangle, ArrowUpRight } from 'lucide-react';
import { Button } from '../../components/ui/Button';
// --- TIPOS ---
interface KpiCardProps {
  icon: React.ReactNode;
  label: string;
  value: string;
  trend: string;
  isPositive: boolean;
}

interface InboxMessage {
  id: number;
  type: 'whatsapp' | 'email';
  preview: string;
  contact: string;
  date: string;
  time: string;
  avatar: string;
}

interface TeamMember {
  name: string;
  closed: number;
  percentage: number;
}

interface RecentActivity {
  id: number;
  icon: React.ReactNode;
  iconBg: string;
  title: string;
  description: string;
  time: string;
}

// --- MOCK DATA ---
const kpiData: KpiCardProps[] = [
  {
    icon: <Users size={22} className="text-blue-600" />,
    label: 'Total de contactos',
    value: '285',
    trend: '+15.0%',
    isPositive: true,
  },
  {
    icon: <Mail size={22} className="text-blue-600" />,
    label: 'Total de mensajes',
    value: '125',
    trend: '+4.1%',
    isPositive: true,
  },
  {
    icon: <Calendar size={22} className="text-blue-600" />,
    label: 'Próximas tareas',
    value: '125',
    trend: '+5.1%',
    isPositive: false,
  },
  {
    icon: <Settings size={22} className="text-blue-600" />,
    label: 'Estado del sistema',
    value: '98.8%',
    trend: '+16.8%',
    isPositive: true,
  },
];

const inboxMessages: InboxMessage[] = [
  { id: 1, type: 'whatsapp', preview: 'Los muros pueden estar investigando distintas fore...', contact: '+54912518001', date: '10/03/2026', time: '10:40s', avatar: 'A' },
  { id: 2, type: 'email', preview: 'Hola! ¿Cómo están? Estuve viendo su página y la enti...', contact: '+54110696128', date: '12/01/2026', time: '10:06m', avatar: 'B' },
  { id: 3, type: 'whatsapp', preview: 'Bueno! Me pasaron tu contacto porque estaban medi...', contact: '+54921804915', date: '10/03/2026', time: '10:50m', avatar: 'C' },
  { id: 4, type: 'email', preview: 'Buenos, ¿cómo estás? Quería retomar la conversación...', contact: 'carlos@gmail.com', date: '07/03/2024', time: '1:32m', avatar: 'D' },
  { id: 5, type: 'email', preview: 'Buenos, gracias por la info. Me gusta la idea del calc...', contact: 'electr_cal@gmail.com', date: '07/03/2024', time: '1:31m', avatar: 'E' },
];

const teamPerformance: TeamMember[] = [
  { name: 'Sarah Jenkins', closed: 248, percentage: 80 },
  { name: 'Marcus Thorne', closed: 192, percentage: 67 },
  { name: 'Elena Rodriguez', closed: 312, percentage: 90 },
  { name: 'Mavid Kim', closed: 144, percentage: 44 },
];

const recentActivity: RecentActivity[] = [
  {
    id: 1,
    icon: <UserPlus size={14} />,
    iconBg: 'bg-blue-100 text-blue-600',
    title: 'Nuevo usuario creado',
    description: 'El vendedor "Jorge Wilson" fue incorporado.',
    time: 'hace 2 minutos',
  },
  {
    id: 2,
    icon: <ArrowUpRight size={14} />,
    iconBg: 'bg-green-100 text-green-600',
    title: 'Etapa del embudo actualizada',
    description: 'Proyecto Alpha se trasladó a En negociación.',
    time: 'hace 45 minutos',
  },
  {
    id: 3,
    icon: <Download size={14} />,
    iconBg: 'bg-purple-100 text-purple-600',
    title: 'Exportación masiva completada',
    description: 'Base de datos de clientes potenciales (TS) importada en CSV por: Alex Sterling.',
    time: 'hace 2 horas',
  },
  {
    id: 4,
    icon: <Tag size={14} />,
    iconBg: 'bg-yellow-100 text-yellow-600',
    title: 'Nueva etiqueta aplicada',
    description: 'Etiqueta global "Alta prioridad" aplicada a 12 picos.',
    time: 'hace 4 minutos',
  },
  {
    id: 5,
    icon: <AlertTriangle size={14} />,
    iconBg: 'bg-red-100 text-red-600',
    title: 'Alerta de seguridad',
    description: 'Intento de inicio de sesión fallido detectado desde IP: 192.168.17.',
    time: 'hace 1 min',
  },
];

// --- SUBCOMPONENTES ---
const KpiCard = ({ icon, label, value, trend, isPositive }: KpiCardProps) => (
  <div className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm flex items-start gap-4">
    <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center shrink-0">
      {icon}
    </div>
    <div className="flex-1 min-w-0">
      <p className="text-slate-500 text-xs font-medium mb-1">{label}</p>
      <p className="text-2xl font-bold text-[#13316b]">{value}</p>
      <span className={`text-xs font-semibold ${isPositive ? 'text-green-500' : 'text-red-500'}`}>
        {trend}
      </span>
    </div>
  </div>
);

const InboxRow = ({ msg }: { msg: InboxMessage }) => (
  <tr className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
    <td className="px-4 py-3">
      <div className="flex items-center gap-3">
        <div className="w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
          {msg.avatar}
        </div>
        <span className="text-slate-600 text-sm truncate max-w-[200px]">{msg.preview}</span>
      </div>
    </td>
    <td className="px-4 py-3 text-slate-500 text-sm">{msg.contact}</td>
    <td className="px-4 py-3 text-slate-500 text-sm">{msg.date}</td>
    <td className="px-4 py-3 text-slate-500 text-sm">{msg.time}</td>
    <td className="px-4 py-3">
      <div className="w-7 h-7 rounded-full bg-slate-100 flex items-center justify-center">
        <Users size={13} className="text-slate-500" />
      </div>
    </td>
  </tr>
);

const TeamBar = ({ member }: { member: TeamMember }) => (
  <div className="mb-4">
    <div className="flex justify-between items-center mb-1">
      <span className="text-sm text-slate-700 font-medium">{member.name}</span>
      <span className="text-xs text-slate-500">{member.closed} Cerradas · {member.percentage}%</span>
    </div>
    <div className="w-full bg-slate-100 rounded-full h-2">
      <div
        className="bg-[#3b82f6] h-2 rounded-full transition-all"
        style={{ width: `${member.percentage}%` }}
      />
    </div>
  </div>
);

const ActivityItem = ({ item }: { item: RecentActivity }) => (
  <div className="flex gap-3 mb-4">
    <div className={`w-7 h-7 rounded-full flex items-center justify-center shrink-0 ${item.iconBg}`}>
      {item.icon}
    </div>
    <div className="flex-1 min-w-0">
      <p className="text-sm font-semibold text-slate-700">{item.title}</p>
      <p className="text-xs text-slate-500 mt-0.5">{item.description}</p>
      <p className="text-xs text-slate-400 mt-1">{item.time}</p>
    </div>
  </div>
);

// --- PÁGINA PRINCIPAL ---
export const AdminPanel = () => {
  return (
    <div>
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Panel</h1>
          <p className="text-slate-500 text-sm mt-0.5">Bienvenido de nuevo, Alex. Esto es lo que está sucediendo hoy.</p>
        </div>
        <div className="flex gap-3">
          <Button variant="outline" size="sm" className="border border-blue-600 text-blue-600 hover:bg-blue-50 flex items-center gap-2">
            <Download size={15} />
            Exportar Reporte
          </Button>
          <Button variant="primary" size="sm" className="flex items-center gap-2">
            <Plus size={15} />
            Nueva Campaña
          </Button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {kpiData.map((kpi, i) => (
          <KpiCard key={i} {...kpi} />
        ))}
      </div>

      {/* Contenido principal */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* Columna izquierda */}
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
                  {inboxMessages.map(msg => (
                    <InboxRow key={msg.id} msg={msg} />
                  ))}
                </tbody>
              </table>
            </div>
            <div className="px-5 py-3 border-t border-slate-100 flex justify-end">
              <button className="text-blue-600 text-sm font-semibold hover:underline">VER TODO</button>
            </div>
          </div>

          {/* Rendimiento del Equipo */}
          <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
            <h2 className="text-base font-bold text-[#13316b] mb-1">Rendimiento del Equipo</h2>
            <p className="text-xs text-slate-500 mb-5">Conversiones cerradas por agente de ventas este mes</p>
            {teamPerformance.map((member, i) => (
              <TeamBar key={i} member={member} />
            ))}
          </div>
        </div>

        {/* Columna derecha — Actividad Reciente */}
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
          <h2 className="text-base font-bold text-[#13316b] mb-5">Actividad Recientes</h2>
          {recentActivity.map(item => (
            <ActivityItem key={item.id} item={item} />
          ))}
          <button className="w-full mt-2 py-2 border border-slate-200 rounded-xl text-sm text-slate-600 hover:bg-slate-50 transition-colors font-medium">
            Ver registro de auditoría
          </button>
        </div>

      </div>
    </div>
  );
};