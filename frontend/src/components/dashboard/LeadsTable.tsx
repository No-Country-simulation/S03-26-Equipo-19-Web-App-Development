import React from "react";
import { Users, MessageSquare, Mail, Trash2, Filter } from "lucide-react";
import { Lead } from "../../types";
import { Badge } from "../ui/Badge";
import { Spinner } from "../ui/Spinner";
import { getInitials } from "../../lib/utils";

interface LeadsTableProps {
  leads: Lead[];
  loading: boolean;
  totalLeads: number;
  onDelete: (id: string) => void;
}

const LeadsTable: React.FC<LeadsTableProps> = ({
  leads,
  loading,
  totalLeads,
  onDelete,
}) => {
  return (
    <div className="bg-white rounded-[2rem] border border-slate-100 shadow-sm overflow-hidden hover:shadow-lg transition-shadow duration-300">
      <div className="p-7 border-b border-slate-50 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div className="flex items-center gap-4">
          <div className="p-2.5 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl shadow-lg shadow-indigo-500/20">
            <Users size={20} className="text-white" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-slate-900">
              Base de Datos de Leads
            </h2>
            <p className="text-sm text-slate-400 font-medium">
              {totalLeads} contactos registrados
            </p>
          </div>
        </div>
        <div className="flex gap-3">
          <button className="flex items-center gap-2 px-5 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-600 hover:bg-slate-50 hover:border-slate-300 transition-all bg-white">
            <Filter size={16} /> Filtros
          </button>
          <button className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-slate-900 text-xs font-bold text-white hover:bg-slate-800 transition-all shadow-lg">
            Exportar CSV
          </button>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50/50">
              <th className="px-8 py-5 text-[11px] font-black text-slate-400 uppercase tracking-widest">
                Contacto
              </th>
              <th className="px-8 py-5 text-[11px] font-black text-slate-400 uppercase tracking-widest">
                Estado
              </th>
              <th className="px-8 py-5 text-[11px] font-black text-slate-400 uppercase tracking-widest">
                Canal
              </th>
              <th className="px-8 py-5 text-[11px] font-black text-slate-400 uppercase tracking-widest"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-50">
            {loading ? (
              <tr>
                <td colSpan={4} className="p-12 text-center">
                  <div className="flex flex-col items-center gap-3">
                    <Spinner />
                    <p className="text-slate-400 font-medium">
                      Cargando datos de la nube...
                    </p>
                  </div>
                </td>
              </tr>
            ) : leads.length === 0 ? (
              <tr>
                <td colSpan={4} className="p-12 text-center">
                  <div className="flex flex-col items-center gap-3">
                    <div className="w-16 h-16 rounded-2xl bg-slate-100 flex items-center justify-center">
                      <Users size={32} className="text-slate-300" />
                    </div>
                    <p className="text-slate-400 font-medium">
                      No hay leads registrados aún.
                    </p>
                  </div>
                </td>
              </tr>
            ) : (
              leads.map((lead) => (
                <tr
                  key={lead.id}
                  className="hover:bg-slate-50/50 transition-all group"
                >
                  <td className="px-8 py-5">
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-indigo-100 to-purple-100 flex items-center justify-center text-sm font-black text-indigo-600 uppercase shadow-inner">
                        {getInitials(lead.name)}
                      </div>
                      <div>
                        <div className="text-base font-bold text-slate-900">
                          {lead.name}
                        </div>
                        <div className="text-sm text-slate-400">
                          {lead.email}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-8 py-5">
                    <Badge status={lead.status} />
                  </td>
                  <td className="px-8 py-5">
                    <div className="flex items-center gap-2 text-sm font-semibold text-slate-600">
                      {lead.channel === "WhatsApp" ? (
                        <div className="w-8 h-8 rounded-lg bg-emerald-100 flex items-center justify-center">
                          <MessageSquare
                            size={16}
                            className="text-emerald-600"
                          />
                        </div>
                      ) : (
                        <div className="w-8 h-8 rounded-lg bg-blue-100 flex items-center justify-center">
                          <Mail size={16} className="text-blue-600" />
                        </div>
                      )}
                      {lead.channel}
                    </div>
                  </td>
                  <td className="px-8 py-5 text-right">
                    <button
                      onClick={() => onDelete(lead.id)}
                      className="p-3 text-slate-300 hover:text-red-500 hover:bg-red-50 rounded-xl transition-all opacity-0 group-hover:opacity-100"
                    >
                      <Trash2 size={18} />
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default LeadsTable;
