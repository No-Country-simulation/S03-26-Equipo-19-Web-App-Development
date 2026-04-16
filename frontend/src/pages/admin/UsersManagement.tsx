import { useState } from 'react';
import { UserPlus, Download, Users, UserCheck, UserX, Filter, Pencil, Trash2 } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { useGetSalespersons, useGetSalespersonById } from '../../services/use_queries/salespersons-query';
import type { SalespersonResponse } from '../../types/admin.types';
import { Modal } from '../../components/ui/Modal';

// NOTA: El Swagger no expone un endpoint /users separado para ADMIN.
// Esta página lista los vendedores (SALESPERSON) via /api/v1/salespersons.
// Si el backend expone /api/v1/users en el futuro, reemplazar el hook por uno dedicado.

const KpiCard = ({ icon, label, value, trend, isPositive, iconBg }: {
  icon: React.ReactNode; label: string; value: string;
  trend: string; isPositive: boolean; iconBg: string;
}) => (
  <div className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm flex items-start gap-4">
    <div className={`w-10 h-10 ${iconBg} rounded-xl flex items-center justify-center shrink-0`}>{icon}</div>
    <div>
      <p className="text-slate-500 text-xs font-medium mb-1">{label}</p>
      <p className="text-2xl font-bold text-[#13316b]">{value}</p>
      <span className={`text-xs font-semibold ${isPositive ? 'text-green-500' : 'text-red-500'}`}>{trend}</span>
    </div>
  </div>
);

const RoleBadge = ({ role }: { role: string }) => (
  <span className={`px-3 py-1 rounded-full text-xs font-bold ${
    role === 'ADMIN' ? 'bg-[#13316b] text-white' : 'bg-[#3b82f6] text-white'
  }`}>
    {role}
  </span>
);

const StatusDot = ({ status }: { status: string }) => (
  <span className="flex items-center gap-1.5 text-sm">
    <span className={`w-2 h-2 rounded-full ${status === 'ACTIVE' ? 'bg-green-500' : 'bg-red-400'}`} />
    <span className={status === 'ACTIVE' ? 'text-green-600' : 'text-red-500'}>
      {status === 'ACTIVE' ? 'Active' : 'Inactive'}
    </span>
  </span>
);

export const UsersManagement = () => {
  const [search, setSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(1);

  const { data: users = [], isLoading, isError } = useGetSalespersons(true);
  const { remove } = useSalespersonsMutations();

  const filtered = users.filter((u: SalespersonResponse) =>
    u.name.toLowerCase().includes(search.toLowerCase()) ||
    u.email.toLowerCase().includes(search.toLowerCase())
  );

  const active = users.filter((u: SalespersonResponse) => u.status === 'ACTIVE');
  const inactive = users.filter((u: SalespersonResponse) => u.status === 'INACTIVE');

  const kpiData = [
    { icon: <Users size={20} className="text-blue-600" />, iconBg: 'bg-blue-50', label: 'Total de usuarios', value: String(users.length), trend: '', isPositive: true },
    { icon: <UserCheck size={20} className="text-blue-600" />, iconBg: 'bg-blue-50', label: 'Usuarios activos', value: String(active.length), trend: '', isPositive: true },
    { icon: <Users size={20} className="text-blue-600" />, iconBg: 'bg-blue-50', label: 'Nuevos usuarios', value: '—', trend: '', isPositive: true },
    { icon: <UserX size={20} className="text-pink-400" />, iconBg: 'bg-pink-50', label: 'Usuarios inactivos', value: String(inactive.length), trend: '', isPositive: false },
  ];

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-[#13316b]">Gestión de Usuarios</h1>
          <p className="text-slate-500 text-sm mt-0.5">
            Supervisar el acceso a la plataforma, asignar roles y administrar permisos del equipo
          </p>
        </div>
        <Button variant="primary" size="md" className="flex items-center gap-2 whitespace-nowrap">
          <UserPlus size={16} /> Crear Usuario
        </Button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {kpiData.map((kpi, i) => <KpiCard key={i} {...kpi} />)}
      </div>

      <div className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 px-5 py-4 border-b border-slate-100">
          <div className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 w-full sm:w-80">
            <Filter size={14} className="text-slate-400 shrink-0" />
            <input
              type="text"
              placeholder="Buscar usuarios por nombre o correo..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="bg-transparent text-sm text-slate-600 placeholder:text-slate-400 outline-none w-full"
            />
          </div>
          <Button variant="primary" size="sm" className="flex items-center gap-2 whitespace-nowrap">
            <Download size={14} /> Exportar
          </Button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 text-slate-400 text-left text-xs uppercase tracking-wide">
                <th className="px-6 py-4 font-medium">Nombre</th>
                <th className="px-6 py-4 font-medium">Correo</th>
                <th className="px-6 py-4 font-medium">Rol</th>
                <th className="px-6 py-4 font-medium">Estado</th>
                <th className="px-6 py-4 font-medium">Acción</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan={5} className="px-6 py-10 text-center text-slate-400">Cargando usuarios...</td></tr>
              ) : isError ? (
                <tr><td colSpan={5} className="px-6 py-10 text-center text-red-400">Error al cargar usuarios</td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan={5} className="px-6 py-10 text-center text-slate-400">No se encontraron usuarios</td></tr>
              ) : (
                filtered.map((user: SalespersonResponse) => {
                  const initials = user.name.split(' ').map((n: string) => n[0]).join('').slice(0, 2).toUpperCase();
                  return (
                    <tr key={user.id} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-9 h-9 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
                            {initials}
                          </div>
                          <div>
                            <p className="font-medium text-slate-800">{user.name}</p>
                            <p className="text-xs text-slate-400">
                              {user.lastActivity ? `Última actividad: ${user.lastActivity}` : ''}
                            </p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-slate-500">{user.email}</td>
                      <td className="px-6 py-4"><RoleBadge role="SALESPERSON" /></td>
                      <td className="px-6 py-4"><StatusDot status={user.status} /></td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <button className="text-blue-500 hover:text-blue-700 transition-colors"><Pencil size={16} /></button>
                          <button
                            className="text-slate-400 hover:text-red-500 transition-colors"
                            onClick={() => remove.mutate(user.id)}
                          >
                            <Trash2 size={16} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        <div className="flex items-center justify-between px-6 py-4 border-t border-slate-100">
          <p className="text-sm text-slate-500">Mostrando {filtered.length} de {users.length} usuarios</p>
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
    </div>
  );
};