import { useState } from 'react';
import { Plus, Search} from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { useGetSavedViews } from '../../services/use_queries/saved-views-query';
import type { SavedViewResponse } from '../../types/admin.types';
import { Modal } from '../../components/ui/Modal';
import { SavedViewForm } from '../../components/saved_views/SavedViewForm';
import { useSavedViewsMutations } from '../../services/use_mutations/saved-views-mutation';
import { ViewCard } from '../../components/saved_views/ViewCard';



export const SavedViewsPage = () => {
  const [search, setSearch] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [editingView, setEditingView] = useState<SavedViewResponse | null>(null);

  const { data: views = [], isLoading, isError } = useGetSavedViews();
  const { create, update, remove } = useSavedViewsMutations();

  const filtered = views.filter((v: SavedViewResponse) =>
    v.name.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Mis vistas guardadas</h1>
          <p className="text-slate-500 text-sm mt-0.5">Accedé rápidamente a tus filtros favoritos</p>
        </div>
        <Button variant="primary" size="md" className="flex items-center gap-2 whitespace-nowrap"
          onClick={() => {
            setEditingView(null);
            setModalOpen(true);
          }}>
          <Plus size={16} /> Nueva vista
        </Button>
      </div>

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

      {isLoading ? (
        <div className="text-center py-16 text-slate-400 text-sm">Cargando vistas...</div>
      ) : isError ? (
        <div className="text-center py-16 text-red-400 text-sm">Error al cargar vistas</div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-16 text-slate-400 text-sm">No se encontraron vistas guardadas</div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {filtered.map((view: SavedViewResponse) => (
            <ViewCard
              key={view.id}
              view={view}
              onDelete={(id) => remove.mutate(id)}
              onEdit={(v) => {
                setEditingView(v);
                setModalOpen(true);
              }}
            />
          ))}
        </div>
      )}

      <div className="mt-12 pt-6 border-t border-slate-200 flex flex-col sm:flex-row justify-between items-center gap-2 text-xs text-slate-400">
        <span>© 2026 Conversa CRM. Todos los derechos reservados.</span>
        <div className="flex gap-4">
          <button className="hover:text-slate-600 transition-colors">Política de Privacidad</button>
          <button className="hover:text-slate-600 transition-colors">Términos y Condiciones de Uso</button>
          <button className="hover:text-slate-600 transition-colors">Preguntas Frecuentes</button>
        </div>
      </div>

      <Modal
        isOpen={modalOpen}
        onClose={() => {
          setModalOpen(false);
          setEditingView(null);
        }}
        title={editingView ? "Editar vista" : "Nueva vista"}
      >
        <SavedViewForm
          initialValues={
            editingView
              ? {
                name: editingView.name,
                entity: editingView.entity,
                filters: editingView.filters,
                sortBy: editingView.sortBy,
                sortOrder: editingView.sortOrder || "ASC",
                global: editingView.isGlobal ?? false,
              }
              : undefined
          }
          onSubmit={(data) => {
            const cleanFilters = Object.fromEntries(
              Object.entries(data.filters).filter(([, v]) => {
                if (Array.isArray(v)) return v.length > 0;
                return v !== "" && v !== null && v !== undefined;
              })
            );

            const payload = { ...data, filters: cleanFilters };

            if (editingView) {
              update.mutate(
                { id: editingView.id, data: payload },
                {
                  onSuccess: () => {
                    setModalOpen(false);
                    setEditingView(null);
                  },
                }
              );
            } else {
              create.mutate(payload, {
                onSuccess: () => setModalOpen(false),
              });
            }
          }}
          onCancel={() => {
            setModalOpen(false);
            setEditingView(null);
          }}       
        />
      </Modal>
    </div>

  );
};