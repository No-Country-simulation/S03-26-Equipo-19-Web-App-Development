import { useState } from 'react';
import { Plus, Filter, Download, Pencil, Trash2 } from 'lucide-react';
import { Button } from '../../components/ui/Button';

// --- TIPOS ---
type FunnelStatus = 'Active' | 'Inactive';

interface FunnelStage {
  id: number;
  name: string;
  order: number;
  status: FunnelStatus;
}

// --- MOCK DATA ---
const mockStages: FunnelStage[] = [
  { id: 1, name: 'Lead', order: 1, status: 'Active' },
  { id: 2, name: 'Contactado', order: 2, status: 'Active' },
  { id: 3, name: 'Interesado', order: 3, status: 'Inactive' },
  { id: 4, name: 'En Negociación', order: 4, status: 'Active' },
  { id: 5, name: 'Cliente', order: 5, status: 'Active' },
  { id: 6, name: 'Perdido', order: 6, status: 'Active' },
];

// --- SUBCOMPONENTES ---
const StatusDot = ({ status }: { status: FunnelStatus }) => (
  <span className="flex items-center gap-1.5 text-sm">
    <span className={`w-2 h-2 rounded-full ${status === 'Active' ? 'bg-green-500' : 'bg-red-400'}`} />
    <span className={status === 'Active' ? 'text-green-600' : 'text-red-500'}>{status}</span>
  </span>
);

// --- PÁGINA PRINCIPAL ---
export const Funnels = () => {
  const [search, setSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const totalStages = 20;

  const filtered = mockStages.filter(s =>
    s.name.toLowerCase().includes(search.toLowerCase()) ||
    s.status.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Gestión del Embudo de Ventas</h1>
          <p className="text-slate-500 text-sm mt-0.5">
            Configurá las etapas del embudo de ventas. Podés reordenarlas y adaptarlas a tu proceso
          </p>
        </div>
        <Button variant="primary" size="md" className="flex items-center gap-2 whitespace-nowrap">
          <Plus size={16} />
          Añadir Etapa
        </Button>
      </div>

      {/* Tabla */}
      <div className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">

        {/* Barra de búsqueda y exportar */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 px-5 py-4 border-b border-slate-100">
          <div className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 w-full sm:w-80">
            <Filter size={14} className="text-slate-400 shrink-0" />
            <input
              type="text"
              placeholder="Buscar por estado activo o inactivo..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="bg-transparent text-sm text-slate-600 placeholder:text-slate-400 outline-none w-full"
            />
          </div>
          <Button variant="primary" size="sm" className="flex items-center gap-2 whitespace-nowrap">
            <Download size={14} />
            Exportar
          </Button>
        </div>

        {/* Tabla de etapas */}
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 text-slate-400 text-left text-xs uppercase tracking-wide">
                <th className="px-6 py-4 font-medium">Etapa</th>
                <th className="px-6 py-4 font-medium">Orden</th>
                <th className="px-6 py-4 font-medium">Estado</th>
                <th className="px-6 py-4 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-6 py-10 text-center text-slate-400">
                    No se encontraron etapas
                  </td>
                </tr>
              ) : (
                filtered.map(stage => (
                  <tr key={stage.id} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4 text-slate-700 font-medium">{stage.name}</td>
                    <td className="px-6 py-4">
                      <span className="w-7 h-7 bg-slate-100 text-slate-600 rounded-full flex items-center justify-center text-xs font-bold">
                        {stage.order}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <StatusDot status={stage.status} />
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <button className="text-blue-500 hover:text-blue-700 transition-colors">
                          <Pencil size={16} />
                        </button>
                        <button className="text-slate-400 hover:text-red-500 transition-colors">
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
            Mostrando 1-6 de {totalStages} etapas
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