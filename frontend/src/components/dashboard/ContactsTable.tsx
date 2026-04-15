import { SlidersHorizontal, Eye, UserRoundPen } from 'lucide-react';
import { timeAgo } from '../../utils/timeAgo';
import type { ContactReqType, ContactResType } from '../../types/contact.types';
import { useNavigate } from 'react-router-dom';
import { getStatusLabel } from '../../utils/formateStatusLabel';
import AvatarContact from '../ui/AvatarContact';
import { Modal } from '../ui/Modal';
import { ContactForm } from '../contacts/ContactForm';
import { useMemo, useState } from 'react';
import { ContactsMutationsService } from '../../services/use_mutations/contacts-mutation';
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
} from "../ui/dropdown-menu";



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

interface ContactsTableProps {
  contacts: ContactResType[];
  isLoading?: boolean;
}

export const ContactsTable = ({ contacts, isLoading }: ContactsTableProps) => {

  const [modalOpen, setModalOpen] = useState(false);
  const [selectedContact, setSelectedContact] = useState<ContactResType | null>(null);
  const [sortOrder, setSortOrder] = useState<"asc" | "desc">("desc");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");

  const navigate = useNavigate()

  const { mutationUpdateContactById } = ContactsMutationsService();

  const updateContact = (data: ContactReqType) => {
    if (!selectedContact) return;

    mutationUpdateContactById.mutate(
      { id: selectedContact.id, data },
      {
        onSuccess: () => {
          setModalOpen(false);
          setSelectedContact(null);
        },
      }
    );
  };

  const processedContacts = useMemo(() => {
    let result = [...contacts];

    // filtro por estado
    if (statusFilter !== "ALL") {
      result = result.filter(
        (c) => c.funnelStatus === statusFilter
      );
    }

    // orden por fecha (updatedAt)
    result.sort((a, b) => {
      const dateA = new Date(a.updatedAt ?? "").getTime();
      const dateB = new Date(b.updatedAt ?? "").getTime();

      return sortOrder === "asc"
        ? dateA - dateB
        : dateB - dateA;
    });

    return result;
  }, [contacts, sortOrder, statusFilter]);

  if (isLoading) return <div className="flex items-center justify-center">
    <p className="text-lg font-medium text-primary">Cargando datos...</p>
  </div>;

  return (
    <>
      <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm h-full flex flex-col">
        <div className='flex justify-between items-center mb-4'>
          <h3 className="text-lg font-bold text-primary">
            Contactos recientes
          </h3>

          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button className="p-2 rounded-md hover:bg-slate-100 transition">
                <SlidersHorizontal size={20} className="text-primary" />
              </button>
            </DropdownMenuTrigger>

            <DropdownMenuContent className="w-56 bg-white">

              <DropdownMenuLabel>Orden</DropdownMenuLabel>
              <DropdownMenuItem onClick={() => setSortOrder("desc")}>
                {sortOrder === "desc" && "✓ "} Más recientes
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setSortOrder("asc")}>
                Más antiguos
              </DropdownMenuItem>

              <DropdownMenuSeparator />

              <DropdownMenuLabel>Estado en funnel</DropdownMenuLabel>
              <DropdownMenuItem onClick={() => setStatusFilter("ALL")}>
                Todos
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setStatusFilter("NEW_LEAD")}>
                Nuevo
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setStatusFilter("CONTACTED")}>
                Contactado
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setStatusFilter("IN_NEGOTIATION")}>
                Negociación
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setStatusFilter("PROPOSAL_SENT")}>
                Propuesta
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setStatusFilter("CLOSED_WON")}>
                Ganado
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setStatusFilter("CLOSED_LOST")}>
                Perdido
              </DropdownMenuItem>

            </DropdownMenuContent>
          </DropdownMenu>
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
              {processedContacts.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-6 text-center text-sm text-neutro-2">
                    No hay datos que coincidan con el filtro
                  </td>
                </tr>
              ) : (
                processedContacts?.map((contact: ContactResType) => (
                  <tr key={contact.id} className="border-b border-slate-50 last:border-0 hover:bg-slate-50 transition-colors text-slate-600">
                    <td className="py-4 px-2 font-medium text-neutro-1 flex items-center gap-3 min-w-0">
                      <AvatarContact name={contact.name} lastName={contact.lastName} size='sm' />
                      <span className="truncate text-xs">{contact.name} {contact.lastName}</span>
                    </td>
                    <td className="py-4 px-2">
                      <span className={`px-3 py-1 rounded-md font-medium text-white text-[10px] whitespace-nowrap ${getFunnelStatusColor(contact.funnelStatus)}`}>
                        {getStatusLabel(contact.funnelStatus)}
                      </span>
                    </td>
                    <td className="py-4 px-2">{timeAgo(contact.updatedAt!)}</td>
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
                        aria-label={`Editar perfil de ${contact.name}`}
                        title="Editar contacto"
                        className={`p-1 rounded-md text-primary hover:bg-secondary hover:text-white transition-colors flex items-center justify-center`}
                        onClick={() => {
                          setSelectedContact(contact);
                          setModalOpen(true);
                        }}
                      >
                        <UserRoundPen size={24} />
                      </button>
                    </td>
                  </tr>
                )))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal para editar contacto */}
      <Modal
        isOpen={modalOpen}
        onClose={() => {
          setModalOpen(false);
          setSelectedContact(null);
        }}
        title="Editar contacto"
      >
        {selectedContact && (
          <ContactForm
            initialData={{
              contact: {
                name: selectedContact.name,
                lastName: selectedContact.lastName,
                email: selectedContact.email,
                phone: selectedContact.phone,
                company: selectedContact.company,
              },
              preferredChannel: selectedContact.preferredChannel,
            }}
            onSubmit={updateContact}
            onCancel={() => {
              setModalOpen(false);
              setSelectedContact(null);
            }}
          />
        )}
      </Modal>
    </>
  );
};