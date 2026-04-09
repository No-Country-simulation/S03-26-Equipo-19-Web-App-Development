import { SlidersHorizontal, Eye, UserRoundPen } from 'lucide-react';
import { getInitials } from '../../utils/getInitials';
import { useGetContacts } from '../../services/use_queries/contacts-query';
import { timeAgo } from '../../utils/timeAgo';
import type { ContactResType } from '../../types/contact.types';
import { useNavigate } from 'react-router-dom';
import { getStatusLabel } from '../../utils/formateStatusLabel';


  
const getFunnelStatusColor = (status: string) => {
  switch (status) {
    case 'NEW_LEAD': return 'bg-accent';
    case 'CONTACTED': return 'bg-secondary ';
    case 'IN_NEGOTIATION': return 'bg-primary';
    case 'PROPOSAL_SENT': return 'bg-primary/70';
    case 'CLOSED_WON': return 'bg-success';
    case 'CLOSED_LOST': return 'bg-error';
    default: return 'bg-neutro-3';
  }
};


export const ContactsTable = () => {
  
  const { data: contactsData, isLoading } = useGetContacts();
  const navigate = useNavigate()

  if (isLoading) return <div className="flex items-center justify-center">
    <p className="text-lg font-medium text-primary">Cargando datos...</p>
  </div>;

  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm h-full flex flex-col">
      <div className='flex justify-between'>
        <h3 className="text-lg font-bold text-primary mb-4">Contactos recientes</h3>
        <SlidersHorizontal size={20} className="text-primary" />
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse text-xs">
          <thead>
            <tr className="border-b border-neutro-2 font-semibold text-neutro-2">
              <th className="py-3 px-2">Nombre</th>
              <th className="py-3 px-2">Estado</th>
              <th className="py-3 px-2">Última interacción</th>
              <th className="py-3 px-2">Etiquetas</th>
              <th className="py-3 px-2 text-right">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {contactsData?.map((contact : ContactResType) => (
              <tr key={contact.id} className="border-b border-slate-50 last:border-0 hover:bg-slate-50 transition-colors text-slate-600">
                <td className="py-4 px-2 font-medium text-neutro-1 flex items-center gap-3 min-w-0">
                  <span className="w-8 h-8 flex-shrink-0 border-2 border-primary rounded-full flex items-center justify-center text-primary font-bold">
                    {getInitials(contact?.name ?? undefined, contact?.lastName ?? undefined)}
                  </span>
                  <span className="truncate text-xs">{contact.name} {contact.lastName}</span>
                </td>
                <td className="py-4 px-2">
                  <span className={`px-3 py-1 rounded-md font-medium text-white text-[10px] whitespace-nowrap ${getFunnelStatusColor(contact.funnelStatus)}`}>
                    {getStatusLabel(contact.funnelStatus)}
                  </span>
                </td>
                <td className="py-4 px-2">{timeAgo(contact.updatedAt)}</td>
                <td className="py-4 px-2 w-[220px] min-w-[220px]">
                  <div className="flex gap-1 justify-start">
                    {contact.tags.map((tag) => (
                      <span
                        key={tag.id}
                        className="px-2 py-0.5 rounded-md text-[10px] font-medium bg-neutro-3 text-primary whitespace-nowrap w-fit"
                      >
                        {tag.name}
                      </span>
                    ))}
                  </div>
                </td>

                <td className="py-2 px-2 flex justify-end gap-3">

                  <button
                    aria-label={`Ver perfil de ${contact.name}`}
                    title="Ver perfil"
                    className={`p-1 rounded-md text-primary hover:bg-secondary hover:text-white transition-colors flex items-center justify-center`}
                     onClick={() => navigate(`/dashboard/contacts/${contact.id}`)}
                  >
                    <Eye size={24} />
                  </button>

                  <button
                    aria-label={`Eliminar a ${contact.name}`}
                    title="Editar contacto"
                    className={`p-1 rounded-md text-primary hover:bg-secondary hover:text-white transition-colors flex items-center justify-center`}
                  >
                    <UserRoundPen size={24} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};