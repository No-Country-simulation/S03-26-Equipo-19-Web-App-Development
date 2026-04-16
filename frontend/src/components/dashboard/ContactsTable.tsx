import { SlidersHorizontal, Eye, UserRoundPen, Trash2 } from 'lucide-react';
import { timeAgo } from '../../utils/timeAgo';
import type { ContactReqType, ContactResType } from '../../types/contact.types';
import { useNavigate } from 'react-router-dom';
import { getStatusLabel } from '../../utils/formateStatusLabel';
import AvatarContact from '../ui/AvatarContact';
import { Modal } from '../ui/Modal';
import { ContactForm } from '../contacts/ContactForm';
import { useMemo, useState } from 'react';
import { ContactsMutationsService } from '../../services/use_mutations/contacts-mutation';
import { SalespersonSelector } from "../contacts/SalespersonSelector";

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
    case 'NEW_LEAD':       return 'bg-accent';
    case 'CONTACTED':      return 'bg-secondary';
    case 'IN_NEGOTIATION': return 'bg-primary';
    case 'PROPOSAL_SENT':  return 'bg-primary/70';
    case 'CLOSED_WON':     return 'bg-success';
    case 'CLOSED_LOST':    return 'bg-error';
    default:               return 'bg-neutro-3';
  }
};

interface ContactsTableProps {
  contacts: ContactResType[];
  isLoading: boolean;
  isAdminView?: boolean;
  onDelete?: (id: number) => void;
}

export const ContactsTable = ({
  contacts,
  isLoading,
  isAdminView = false,
  onDelete
}: ContactsTableProps) => {

  const [modalOpen, setModalOpen] = useState(false);
  const [selectedContact, setSelectedContact] = useState<ContactResType | null>(null);
  const [sortOrder, setSortOrder] = useState<"asc" | "desc">("desc");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");

  const navigate = useNavigate();

  const {
    mutationUpdateContactById,
    mutationAssignContact
  } = ContactsMutationsService();

  // Actualizar contacto
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

  // Reasignar contacto
  const handleAssign = async (contactId: number, newOwnerId: number) => {
    try {
      await mutationAssignContact.mutateAsync({ contactId, newOwnerId });
    } catch (error) {
      console.error("Error al reasignar:", error);
    }
  };

  // Filtros + orden
  const processedContacts = useMemo(() => {
    let result = [...contacts];

    if (statusFilter !== "ALL") {
      result = result.filter((c) => c.funnelStatus === statusFilter);
    }

    result.sort((a, b) => {
      const dateA = new Date(a.updatedAt ?? "").getTime();
      const dateB = new Date(b.updatedAt ?? "").getTime();
      return sortOrder === "asc" ? dateA - dateB : dateB - dateA;
    });

    return result;
  }, [contacts, sortOrder, statusFilter]);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-8">
        <p className="text-lg font-medium text-primary">Cargando datos...</p>
      </div>
    );
  }

  // Total de columnas: Admin 6 columnas, Vendedor 5 columnas
  const totalColumns = isAdminView ? 6 : 5;

  return (
    <>
      <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm h-full flex flex-col">
        
        {/* HEADER */}
        <div className='flex justify-between items-center mb-4'>
          <h3 className="text-lg font-bold text-primary">
            {isAdminView ? "Todos los contactos" : "Mis contactos"}
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
                {sortOrder === "desc" && "✓ "}Más recientes
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setSortOrder("asc")}>
                Más antiguos
              </DropdownMenuItem>

              <DropdownMenuSeparator />

              <DropdownMenuLabel>Estado en funnel</DropdownMenuLabel>
              <DropdownMenuItem onClick={() => setStatusFilter("ALL")}>Todos</DropdownMenuItem>
              <DropdownMenuItem onClick={() => setStatusFilter("NEW_LEAD")}>Nuevo</DropdownMenuItem>
              <DropdownMenuItem onClick={() => setStatusFilter("CONTACTED")}>Contactado</DropdownMenuItem>
              <DropdownMenuItem onClick={() => setStatusFilter("IN_NEGOTIATION")}>Negociación</DropdownMenuItem>
              <DropdownMenuItem onClick={() => setStatusFilter("PROPOSAL_SENT")}>Propuesta</DropdownMenuItem>
              <DropdownMenuItem onClick={() => setStatusFilter("CLOSED_WON")}>Ganado</DropdownMenuItem>
              <DropdownMenuItem onClick={() => setStatusFilter("CLOSED_LOST")}>Perdido</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        {/* TABLE */}
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-neutro-2 font-semibold text-neutro-2">
                <th className="py-3 px-2">Nombre</th>
                <th className="py-3 px-2">Estado</th>
                <th className="py-3 px-2">Última interacción</th>
                <th className="py-3 px-2">Etiquetas</th>
                {/* SOLO UNA COLUMNA para Vendedor asignado */}
                {isAdminView && (
                  <th className="py-3 px-2">Vendedor asignado</th>
                )}
                <th className="py-3 px-2 text-right">Acciones</th>
              </tr>
            </thead>

            <tbody>
               {isLoading ? (
                <div className="flex items-center justify-center pt-5 col-span-full">
                  <p className="text-sm font-medium text-primary animate-pulse">Cargando contactos...</p>
                </div>
              ) : (
              processedContacts.length === 0 ? (
                <tr>
                  <td colSpan={totalColumns} className="py-6 text-center text-sm text-neutro-2">
                    No hay datos que coincidan con el filtro
                  </td>
                </tr>
              ) : (
                processedContacts.map((contact) => (
                  <tr
                    key={contact.id}
                    className="border-b border-slate-50 hover:bg-slate-50 transition-colors text-slate-600"
                  >
                    {/* Nombre */}
                    <td className="py-4 px-2">
                      <div className="flex items-center gap-3">
                        <AvatarContact name={contact.name} lastName={contact.lastName} size='sm' />
                        <span className="font-medium">{contact.name} {contact.lastName}</span>
                      </div>
                    </td>

                    {/* Estado */}
                    <td className="py-4 px-2">
                      <span className={`px-3 py-1 rounded-md text-white text-[10px] ${getFunnelStatusColor(contact.funnelStatus)}`}>
                        {getStatusLabel(contact.funnelStatus)}
                      </span>
                    </td>

                    {/* Fecha */}
                    <td className="py-4 px-2 text-gray-500">
                      {timeAgo(contact.updatedAt!)}
                    </td>

                    {/* Tags */}
                    <td className="py-4 px-2">
                      <div className="flex flex-wrap gap-1">
                        {contact.tags && contact.tags.length > 0 ? (
                          contact.tags.map((tag) => (
                            <span key={tag.id} className="px-2 py-0.5 rounded-md text-[10px] bg-gray-100 text-gray-600">
                              {tag.name}
                            </span>
                          ))
                        ) : (
                          <span className="text-gray-400 text-[10px]">-</span>
                        )}
                      </div>
                    </td>

                    {/* ADMIN: Vendedor asignado EDITABLE - UNA SOLA COLUMNA */}
                    {isAdminView && (
                      <td className="py-4 px-2">
                        <SalespersonSelector
                          contactId={contact.id}
                          currentOwnerId={contact.owner?.id}
                          currentOwnerName={contact.owner?.name || "Sin asignar"}
                          onAssign={handleAssign}
                          isAssigning={mutationAssignContact.isPending}
                        />
                      </td>
                    )}

                    {/* Acciones */}
                    <td className="py-2 px-2">
                      <div className="flex justify-end gap-3">
                        <button 
                          onClick={() => navigate(`/dashboard/contacts/${contact.id}`)}
                          className="text-blue-600 hover:text-blue-800 transition-colors"
                          title="Ver detalle"
                        >
                          <Eye size={20} />
                        </button>

                        <button
                          onClick={() => {
                            setSelectedContact(contact);
                            setModalOpen(true);
                          }}
                          className="text-green-600 hover:text-green-800 transition-colors"
                          title="Editar contacto"
                        >
                          <UserRoundPen size={20} />
                        </button>

                        {isAdminView && onDelete && (
                          <button
                            onClick={() => onDelete(contact.id)}
                            className="text-red-600 hover:text-red-800 transition-colors"
                            title="Eliminar contacto"
                          >
                            <Trash2 size={20} />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* MODAL EDITAR CONTACTO */}
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