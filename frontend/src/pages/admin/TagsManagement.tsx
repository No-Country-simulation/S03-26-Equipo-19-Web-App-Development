import { useState } from 'react';
import { Plus, Pencil, Trash2, Tag } from 'lucide-react';
import { Button } from '../../components/ui/Button';

// --- TIPOS ---
interface TagItem {
  id: number;
  name: string;
  color: string;
}

interface KpiCardProps {
  icon: React.ReactNode;
  label: string;
  value: string;
  trend?: string;
  isPositive?: boolean;
  subtitle?: string;
}

// --- MOCK DATA ---
const kpiData: KpiCardProps[] = [
  {
    icon: <Tag size={20} className="text-blue-600" />,
    label: 'Total de etiquetas',
    value: '150',
    trend: '+15.0%',
    isPositive: true,
  },
  {
    icon: <Tag size={20} className="text-blue-600" />,
    label: 'Uso de etiquetas',
    value: '84%',
    trend: '+1.5%',
    isPositive: true,
  },
  {
    icon: <Tag size={20} className="text-orange-400" />,
    label: 'Etiquetas sin uso',
    value: '09',
    trend: '+8.5%',
    isPositive: false,
  },
  {
    icon: <Tag size={20} className="text-blue-600" />,
    label: 'Etiqueta más usada',
    value: '',
    subtitle: 'Cliente potencial',
  },
];

const mockTags: TagItem[] = [
  { id: 1, name: 'Cliente potencial', color: '#22c55e' },
  { id: 2, name: 'Seguimiento pendiente', color: '#f59e0b' },
  { id: 3, name: 'Interesado en promoción', color: '#3b82f6' },
  { id: 4, name: 'Compra reciente', color: '#8b5cf6' },
  { id: 5, name: 'Riesgo de abandono', color: '#ef4444' },
];

// --- SUBCOMPONENTES ---
const KpiCard = ({ icon, label, value, trend, isPositive, subtitle }: KpiCardProps) => (
  <div className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm flex items-start gap-4">
    <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center shrink-0">
      {icon}
    </div>
    <div>
      <p className="text-slate-500 text-xs font-medium mb-1">{label}</p>
      {value ? (
        <p className="text-2xl font-bold text-[#13316b]">{value}</p>
      ) : (
        <p className="text-base font-bold text-[#13316b]">{subtitle}</p>
      )}
      {trend && (
        <span className={`text-xs font-semibold ${isPositive ? 'text-green-500' : 'text-red-500'}`}>
          {trend}
        </span>
      )}
    </div>
  </div>
);

// --- PÁGINA PRINCIPAL ---
export const TagsManagement = () => {
  const [tags, setTags] = useState<TagItem[]>(mockTags);
  const [currentPage, setCurrentPage] = useState(1);
  const totalTags = 125;

  const handleDelete = (id: number) => {
    setTags(prev => prev.filter(t => t.id !== id));
  };

  return (
    <div>
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Gestión de Etiquetas</h1>
          <p className="text-slate-500 text-sm mt-0.5 max-w-sm">
            Clasifique y segmenta contactos con etiquetas dinámicas para una gestión clara y eficiente.
          </p>
        </div>
        <Button variant="primary" size="md" className="flex items-center gap-2 whitespace-nowrap">
          <Plus size={16} />
          Crear Etiqueta
        </Button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {kpiData.map((kpi, i) => (
          <KpiCard key={i} {...kpi} />
        ))}
      </div>

      {/* Tabla */}
      <div className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 text-slate-400 text-left text-xs uppercase tracking-wide">
                <th className="px-6 py-4 font-medium">Nombre</th>
                <th className="px-6 py-4 font-medium">Color</th>
                <th className="px-6 py-4 font-medium">Acción</th>
              </tr>
            </thead>
            <tbody>
              {tags.length === 0 ? (
                <tr>
                  <td colSpan={3} className="px-6 py-10 text-center text-slate-400">
                    No hay etiquetas registradas
                  </td>
                </tr>
              ) : (
                tags.map(tag => (
                  <tr key={tag.id} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <Tag size={15} style={{ color: tag.color }} />
                        <span className="text-slate-700 font-medium">{tag.name}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className="w-4 h-4 rounded-full inline-block"
                        style={{ backgroundColor: tag.color }}
                      />
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <button className="text-blue-500 hover:text-blue-700 transition-colors">
                          <Pencil size={16} />
                        </button>
                        <button
                          className="text-slate-400 hover:text-red-500 transition-colors"
                          onClick={() => handleDelete(tag.id)}
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Paginación */}
        <div className="flex items-center justify-between px-6 py-4 border-t border-slate-100">
          <p className="text-sm text-slate-500">
            Mostrando 5 de {totalTags} etiquetas registradas
          </p>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
              disabled={currentPage === 1}
              className="px-4 py-2 rounded-lg text-sm font-medium bg-slate-100 text-slate-600 hover:bg-slate-200 disabled:opacity-30 transition-colors"
            >
              Anterior
            </button>
            <button
              onClick={() => setCurrentPage(p => p + 1)}
              className="px-4 py-2 rounded-lg text-sm font-medium bg-[#13316b] text-white hover:bg-[#0f2557] transition-colors"
            >
              Siguiente
            </button>
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