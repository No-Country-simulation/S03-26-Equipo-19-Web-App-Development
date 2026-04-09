import { useState } from 'react';
import { Plus, Search, Trash2, Pencil } from 'lucide-react';
import { Button } from '../../components/ui/Button';

// --- TIPOS ---
type ViewEntity = 'contacts' | 'tasks';

interface SavedView {
  id: number;
  name: string;
  entity: ViewEntity;
  priority?: string;
  isDefault?: boolean;
  filters: {
    status?: string;
    tags?: string[];
    order?: string;
    type?: string;
  };
  creator: string;
}

// --- MOCK DATA ---
const mockViews: SavedView[] = [
  {
    id: 1,
    name: 'Leads calientes',
    entity: 'contacts',
    priority: 'Alta prioridad',
    isDefault: true,
    filters: { status: 'En negociación' },
    creator: 'Alex Sterling',
  },
  {
    id: 2,
    name: 'Sin movimiento en 7 días',
    entity: 'contacts',
    priority: 'Alta prioridad',
    filters: { status: 'En negociación' },
    creator: 'Alex Sterling',
  },
  {
    id: 3,
    name: 'Clientes recientes',
    entity: 'contacts',
    priority: 'Prioridad',
    filters: { status: 'Contactado', tags: ['Etiqueta 1', 'Etiqueta 2'] },
    creator: 'Sara Martínez',
  },
  {
    id: 4,
    name: 'Seguimiento pendiente',
    entity: 'contacts',
    priority: 'Prioridad',
    filters: { status: 'Contactado', tags: ['Etiqueta 1', 'Etiqueta 2'] },
    creator: 'Marcos Nodal',
  },
  {
    id: 5,
    name: 'Contactos activos',
    entity: 'contacts',
    priority: 'Prioridad',
    filters: { status: 'Contactado', order: 'Por fecha de actividad' },
    creator: 'Elena Rodríguez',
  },
  {
    id: 6,
    name: 'Tareas de seguimiento',
    entity: 'tasks',
    priority: 'Prioridad',
    filters: { type: 'Contactado', tags: ['Etiqueta 1', 'Etiqueta 2'] },
    creator: 'Alex Sterling',
  },
];

// --- SUBCOMPONENTES ---
const FilterTag = ({ label, value }: { label: string; value: string }) => (
  <span className="inline-flex items-center gap-1 bg-slate-100 text-slate-600 text-xs px-2 py-1 rounded-full">
    <span className="font-medium text-slate-400">{label}</span>
    {value}
  </span>
);

const ViewCard = ({ view, onDelete }: { view: SavedView; onDelete: (id: number) => void }) => (
  <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 flex flex-col justify-between gap-4 hover:shadow-md transition-shadow">
    <div>
      {/* Título y badge default */}
      <div className="flex items-center gap-2 mb-1">
        <h3 className="text-sm font-bold text-[#13316b]">{view.name}</h3>
        {view.isDefault && (
          <span className="px-2 py-0.5 bg-blue-100 text-blue-700 text-xs font-semibold rounded-full">
            Default
          </span>
        )}
      </div>

      {/* Prioridad */}
      {view.priority && (
        <p className="text-xs text-slate-400 mb-3">{view.priority}</p>
      )}

      {/* Filtros aplicados */}
      <div className="flex flex-wrap gap-2">
        {view.filters.status && (
          <FilterTag label="Estado" value={view.filters.status} />
        )}
        {view.filters.type && (
          <FilterTag label="Tarea" value={view.filters.type} />
        )}
        {view.filters.order && (
          <FilterTag label="Orden" value={view.filters.order} />
        )}
        {view.filters.tags?.map((tag, i) => (
          <FilterTag key={i} label="Etiquetas" value={tag} />
        ))}
      </div>
    </div>

    {/* Footer */}
    <div className="flex items-center justify-between pt-3 border-t border-slate-100">
      <span className="text-xs text-slate-400">
        <span className="font-medium text-slate-500">Creador</span>
        {' '}{view.creator}
      </span>
      <div className="flex items-center gap-2">
        <button className="text-blue-500 hover:text-blue-700 transition-colors">
          <Pencil size={14} />
        </button>
        <button
          className="text-slate-400 hover:text-red-500 transition-colors"
          onClick={() => onDelete(view.id)}
        >
          <Trash2 size={14} />
        </button>
      </div>
    </div>
  </div>
);

// --- PÁGINA PRINCIPAL ---
export const SavedViewsPage = () => {
  const [views, setViews] = useState<SavedView[]>(mockViews);
  const [search, setSearch] = useState('');

  const filtered = views.filter(v =>
    v.name.toLowerCase().includes(search.toLowerCase())
  );

  const handleDelete = (id: number) => {
    setViews(prev => prev.filter(v => v.id !== id));
  };

  return (
    <div>
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Mis vistas guardadas</h1>
          <p className="text-slate-500 text-sm mt-0.5">
            Accedé rápidamente a tus filtros favoritos
          </p>
        </div>
        <Button variant="primary" size="md" className="flex items-center gap-2 whitespace-nowrap">
          <Plus size={16} />
          Nueva vista
        </Button>
      </div>

      {/* Buscador */}
      <div className="flex items-center gap-2 bg-white border border-slate-200 rounded-xl px-4 py-2.5 w-full sm:w-80 mb-6 shadow-sm">
        <Search size={14} className="text-slate-400 shrink-0" />
        <input
          type="text"
          placeholder="Buscar vista..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="bg-transparent text-sm text-slate-600 placeholder:text-slate-400 outline-none w-full"
        />
      </div>

      {/* Grid de cards */}
      {filtered.length === 0 ? (
        <div className="text-center py-16 text-slate-400 text-sm">
          No se encontraron vistas guardadas
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {filtered.map(view => (
            <ViewCard key={view.id} view={view} onDelete={handleDelete} />
          ))}
        </div>
      )}

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