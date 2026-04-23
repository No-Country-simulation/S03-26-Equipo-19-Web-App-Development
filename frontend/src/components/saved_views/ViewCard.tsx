import { Pencil, Trash2 } from "lucide-react";
import type { SavedViewResponse } from "../../types/admin.types";

const getEntity = (entity: string) => {
  switch (entity) {
    case 'CONTACTS': return 'CONTACTOS';
    case 'TASKS': return 'TAREAS';
  }
};

const FilterTag = ({ label, value }: { label: string; value: string }) => (
  <span className="inline-flex items-center gap-1 bg-slate-100 text-slate-600 text-xs px-2 py-1 rounded-full">
    <span className="font-medium text-slate-400">{label}</span>
    {value}
  </span>
);

type ViewCardProps = {
  view: SavedViewResponse;
  onDelete: (id: number) => void;
  onEdit: (view: SavedViewResponse) => void;
};

export const ViewCard = ({ view, onDelete, onEdit }: ViewCardProps) => {
  const filters = view.filters as Record<string, unknown>;

  return (
    <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 flex flex-col justify-between gap-4 hover:shadow-md transition-shadow">
      <div>
        <div className="flex items-center gap-2 mb-1">
          <h3 className="text-sm font-bold text-primary">{view.name}</h3>
          {view.isDefault && (
            <span className="px-2 py-0.5 bg-blue-100 text-blue-700 text-xs font-semibold rounded-full">Default</span>
          )}
          {view.isGlobal && (
            <span className="px-2 py-0.5 bg-purple-100 text-purple-700 text-xs font-semibold rounded-full">Global</span>
          )}
        </div>

        <p className="text-xs text-slate-400 mb-3 capitalize">{getEntity(view.entity)}</p>

        <div className="flex flex-wrap gap-2">
          {Object.entries(filters).map(([key, val]) =>
            val ? (
              Array.isArray(val)
                ? (val as string[]).map((v, i) => <FilterTag key={`${key}-${i}`} label={key} value={v} />)
                : <FilterTag key={key} label={(key)} value={String(val)} />
            ) : null
          )}
        </div>
      </div>

      <div className="flex items-center justify-between pt-3 border-t border-slate-100">
        <span className="text-xs text-slate-400">
          <span className="font-medium text-slate-500">Creador</span> {view.creator}
        </span>
        <div className="flex items-center gap-2">
          <button className="text-blue-500 hover:text-blue-700 transition-colors" onClick={() => onEdit(view)}><Pencil size={14} /></button>
          <button className="text-slate-400 hover:text-red-500 transition-colors" onClick={() => onDelete(view.id)}>
            <Trash2 size={14} />
          </button>
        </div>
      </div>
    </div>
  );
};