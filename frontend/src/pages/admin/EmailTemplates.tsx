import { useState } from 'react';
import { Plus, Mail, Pencil, Trash2, Star } from 'lucide-react';
import { Button } from '../../components/ui/Button';

// --- TIPOS ---
interface EmailTemplate {
  id: number;
  title: string;
  preview: string;
  updatedAt: string;
  featured?: boolean;
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
    icon: <Mail size={20} className="text-blue-600" />,
    label: 'Total de plantillas',
    value: '289',
    trend: '+15.0%',
    isPositive: true,
  },
  {
    icon: <Mail size={20} className="text-pink-500" />,
    label: 'Plantillas creadas este mes',
    value: '+9',
    trend: '+8.0%',
    isPositive: true,
  },
  {
    icon: <Mail size={20} className="text-blue-600" />,
    label: 'Plantillas creadas hoy',
    value: '4',
    trend: '+5.0%',
    isPositive: true,
  },
  {
    icon: <Mail size={20} className="text-blue-600" />,
    label: 'Promedio diario',
    value: '0.8',
    trend: '+1.06%',
    isPositive: true,
  },
];

const mockTemplates: EmailTemplate[] = [
  {
    id: 1,
    title: 'Bienvenida',
    preview: 'Hola {{first_name}}, bienvenido/a a nuestro ecosistema. Estamos encantados de tener. Te contamos que a futuro...',
    updatedAt: 'Actualizado el 12 de octubre',
  },
  {
    id: 2,
    title: 'Seguimiento de llamadas',
    preview: 'Basado en nuestra conversación, he descrito los pasos a seguir para...',
    updatedAt: 'Actualizado el 06 de octubre',
  },
  {
    id: 3,
    title: 'Revisión trimestral',
    preview: 'Al cerrar la Q3, quería compartir algunas métricas...',
    updatedAt: 'Actualizado el 14 de octubre',
  },
  {
    id: 4,
    title: 'Lanzamiento del proyecto',
    preview: 'Los planos son definitivos. Hoy inicia la construcción de nuestra primera trinchera...',
    updatedAt: 'Actualizado el 05 de octubre',
  },
  {
    id: 5,
    title: 'Invitación de referencia VIP',
    preview: 'Invitación exclusiva para {{primer_nombre}}. Valoramos tu dedicación y deseo entender aún...',
    updatedAt: 'Actualizado el 05 de septiembre',
    featured: true,
  },
  {
    id: 6,
    title: 'Seguimiento de llamadas',
    preview: 'Basado en nuestra conversación, he descrito los pasos a seguir para...',
    updatedAt: 'Actualizado el 30 de octubre',
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

const TemplateCard = ({ template, onDelete }: { template: EmailTemplate; onDelete: (id: number) => void }) => (
  <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 flex flex-col justify-between gap-4 hover:shadow-md transition-shadow">
    <div>
      <div className="flex items-center gap-2 mb-3">
        {template.featured ? (
          <Star size={16} className="text-yellow-400 fill-yellow-400" />
        ) : (
          <Mail size={16} className="text-blue-400" />
        )}
        <h3 className="text-sm font-bold text-[#13316b]">{template.title}</h3>
      </div>
      <p className="text-xs text-slate-500 leading-relaxed line-clamp-3">
        {template.preview}
      </p>
    </div>
    <div className="flex items-center justify-between pt-3 border-t border-slate-100">
      <span className="text-xs text-slate-400">{template.updatedAt}</span>
      <div className="flex items-center gap-3">
        <button className="text-blue-500 hover:text-blue-700 transition-colors">
          <Pencil size={15} />
        </button>
        <button
          className="text-slate-400 hover:text-red-500 transition-colors"
          onClick={() => onDelete(template.id)}
        >
          <Trash2 size={15} />
        </button>
      </div>
    </div>
  </div>
);

// --- PÁGINA PRINCIPAL ---
export const EmailTemplates = () => {
  const [templates, setTemplates] = useState<EmailTemplate[]>(mockTemplates);
  const [currentPage, setCurrentPage] = useState(1);
  const totalTemplates = 34;

  const handleDelete = (id: number) => {
    setTemplates(prev => prev.filter(t => t.id !== id));
  };

  return (
    <div>
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Plantillas de Correo Electrónico</h1>
          <p className="text-slate-500 text-sm mt-0.5 max-w-sm">
            Administra plantillas de correo electrónico optimizadas para mejorar la comunicación y conversión
          </p>
        </div>
        <Button variant="primary" size="md" className="flex items-center gap-2 whitespace-nowrap">
          <Plus size={16} />
          Crear Plantilla
        </Button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {kpiData.map((kpi, i) => (
          <KpiCard key={i} {...kpi} />
        ))}
      </div>

      {/* Grid de plantillas */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
        {templates.map(template => (
          <TemplateCard key={template.id} template={template} onDelete={handleDelete} />
        ))}
      </div>

      {/* Paginación */}
      <div className="flex items-center justify-between">
        <p className="text-sm text-slate-500">
          Mostrando 6 de {totalTemplates} plantillas de correo electrónico
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