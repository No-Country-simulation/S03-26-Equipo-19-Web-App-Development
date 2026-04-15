import { useState } from 'react';
import { Plus, Pencil, Trash2, Tag } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Modal } from '../../components/ui/Modal';
import {TagsForm} from '../../components/tags/TagsForm';
import { useGetTags } from '../../services/use_queries/tags-query';
import type { TagResponse } from '../../types/admin.types';
import { useTagsMutations } from '../../services/use_mutations/tags-mutation';

const KpiCard = ({ icon, label, value, trend, isPositive, subtitle }: {
  icon: React.ReactNode; label: string; value?: string;
  trend?: string; isPositive?: boolean; subtitle?: string;
}) => (
  <div className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm flex items-start gap-4">
    <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center shrink-0">{icon}</div>
    <div>
      <p className="text-slate-500 text-xs font-medium mb-1">{label}</p>
      {value ? (
        <p className="text-2xl font-bold text-[#13316b]">{value}</p>
      ) : (
        <p className="text-base font-bold text-[#13316b]">{subtitle}</p>
      )}
      {trend && (
        <span className={`text-xs font-semibold ${isPositive ? 'text-green-500' : 'text-red-500'}`}>{trend}</span>
      )}
    </div>
  </div>
);

export const TagsManagement = () => {
  const [currentPage, setCurrentPage] = useState(1);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedTag, setSelectedTag] = useState<TagResponse | null>(null);
  const { data: tags = [], isLoading, isError } = useGetTags();
  const { remove: removeTag, create, update} = useTagsMutations();

  const unusedEstimate = tags.length > 0 ? Math.floor(tags.length * 0.06) : 0;
  const mostUsed = tags[0]?.name ?? '—';

  const kpiData = [
    { icon: <Tag size={20} className="text-blue-600" />, label: 'Total de etiquetas', value: String(tags.length), trend: '', isPositive: true },
    { icon: <Tag size={20} className="text-blue-600" />, label: 'Uso de etiquetas', value: '—', trend: '', isPositive: true },
    { icon: <Tag size={20} className="text-orange-400" />, label: 'Etiquetas sin uso', value: String(unusedEstimate), trend: '', isPositive: false },
    { icon: <Tag size={20} className="text-blue-600" />, label: 'Etiqueta más usada', value: '', subtitle: mostUsed },
  ];

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Gestión de Etiquetas</h1>
          <p className="text-slate-500 text-sm mt-0.5 max-w-sm">
            Clasifique y segmenta contactos con etiquetas dinámicas para una gestión clara y eficiente.
          </p>
        </div>
        <Button variant="primary" size="md" className="flex items-center gap-2 whitespace-nowrap" onClick={() => {
          setSelectedTag(null);
          setModalOpen(true);
        }}>
          <Plus size={16} /> Crear Etiqueta
        </Button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {kpiData.map((kpi, i) => <KpiCard key={i} {...kpi} />)}
      </div>

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
              {isLoading ? (
                <tr><td colSpan={3} className="px-6 py-10 text-center text-slate-400">Cargando etiquetas...</td></tr>
              ) : isError ? (
                <tr><td colSpan={3} className="px-6 py-10 text-center text-red-400">Error al cargar etiquetas</td></tr>
              ) : tags.length === 0 ? (
                <tr><td colSpan={3} className="px-6 py-10 text-center text-slate-400">No hay etiquetas registradas</td></tr>
              ) : (
                tags.map((tag: TagResponse) => (
                  <tr key={tag.id} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <Tag size={15} style={{ color: tag.color }} />
                        <span className="text-slate-700 font-medium">{tag.name}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="w-4 h-4 rounded-full inline-block" style={{ backgroundColor: tag.color }} />
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <button onClick={() => { setSelectedTag(tag); setModalOpen(true); }} className="text-blue-500 hover:text-blue-700 transition-colors"><Pencil size={16} /></button>
                        <button
                          className="text-slate-400 hover:text-red-500 transition-colors"
                          onClick={() => removeTag.mutate(tag.id)}
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

        <div className="flex items-center justify-between px-6 py-4 border-t border-slate-100">
          <p className="text-sm text-slate-500">Mostrando {tags.length} etiquetas registradas</p>
          <div className="flex items-center gap-2">
            <button onClick={() => setCurrentPage(p => Math.max(1, p - 1))} disabled={currentPage === 1}
              className="px-4 py-2 rounded-lg text-sm font-medium bg-slate-100 text-slate-600 hover:bg-slate-200 disabled:opacity-30 transition-colors">Anterior</button>
            <button onClick={() => setCurrentPage(p => p + 1)}
              className="px-4 py-2 rounded-lg text-sm font-medium bg-[#13316b] text-white hover:bg-[#0f2557] transition-colors">Siguiente</button>
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

<Modal
  isOpen={modalOpen}
  onClose={() => {
    setModalOpen(false);
    setSelectedTag(null);
  }}
  title={selectedTag ? "Editar Etiqueta" : "Nueva Etiqueta"}
>
  {selectedTag ? (
    <TagsForm
      initialValues={selectedTag}
      onSubmit={(data) => {
        update.mutate(
          { id: selectedTag.id, data },
          {
            onSuccess: () => {
              setModalOpen(false);
              setSelectedTag(null);
            },
          }
        );
      }}
      onCancel={() => {
        setModalOpen(false);
        setSelectedTag(null);
      }}
    />
  ) : (
    <TagsForm
      onSubmit={(data) => {
        create.mutate(data, {
          onSuccess: () => setModalOpen(false),
        });
      }}
      onCancel={() => setModalOpen(false)}
    />
  )}
</Modal>
    </div>
  );
};