import { SlidersHorizontal, MessageCircleMore, Mail, UserRoundPlus } from 'lucide-react';
import type { MessageResType } from '../../types/message.types';
import { formatDateTime } from '../../utils/formateDate';
import { useContactsStore } from '../../store/useContactsStore';
import { useState } from 'react';
import { Modal } from '../ui/Modal';
import { ContactForm } from '../contacts/ContactForm';


const DATA: MessageResType[] = [
  {
    id: 1,
    userId: 6,
    channel: "whatsapp",
    direction: "inbound",
    content: "Hola, estoy interesado en sus servicios. ¿Podrían darme más detalles?",
    createdAt: new Date(),
    externalId: "2",
    messageType: "text",
    status: "delivered",
    conversationId: 1,
  },
  {
    id: 2,
    userId: 1,
    channel: "email",
    direction: "inbound",
    content: "Hola, estoy interesado en sus servicios. ¿Podrían darme más detalles?",
    createdAt: new Date(),
    externalId: "2",
    messageType: "text",
    status: "delivered",
    conversationId: 1,
  },

]



export const InboxTable = () => {

  /*   const { data: messagesData, isLoading } = useGetMessages();
  
    if (isLoading) return <div className="flex items-center justify-center">
      <p className="text-lg font-medium text-primary">Cargando datos...</p>
    </div>; */

  const [modalOpen, setModalOpen] = useState(false);

  const addContact = useContactsStore((state) => state.addContact);


  return (
    <>
      <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm h-full flex flex-col">
        <div className='flex justify-between'>
          <h3 className="text-lg font-bold text-primary mb-4">Bandeja de entrada</h3>
          <SlidersHorizontal size={20} className="text-primary" />
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-neutro-2 font-semibold text-neutro-2">
                <th className="py-3 px-1">Canal</th>
                <th className="py-3 px-1">Mensaje</th>
                <th className="py-3 px-1">Email / Teléfono</th>
                <th className="py-3 px-1">Fecha y Hora</th>
                <th className="py-3 px-1 text-right">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {DATA?.map((message: MessageResType) => (
                <tr key={message.id} className="border-b border-slate-50 last:border-0 hover:bg-slate-50 transition-colors text-slate-600">
                  <td className="py-4 px-1 font-medium text-neutro-1 flex items-center gap-3 min-w-0">
                    <span className="">
                      {message?.channel === 'whatsapp' ? <MessageCircleMore size={24} className='text-success' /> : <Mail size={24} className='text-primary' />}
                    </span>

                  </td>
                  <td className="py-4 px-1">
                    <span className={`px-1 py-1 rounded-md font-medium whitespace-nowrap `}>
                      {message.content.length > 30 ? message.content.substring(0, 30) + '...' : message.content}
                    </span>
                  </td>
                  <td className="py-4 px-2 w-[220px] min-w-[220px]">
                    {message?.channel === 'whatsapp' ? <span className="text-sm text-slate-500">+54 9 11 1234-5678</span> : <span className="text-sm text-slate-500">john.doe@example.com</span>}
                  </td>
                  <td className="py-4 px-2">{formatDateTime(message?.createdAt)}hs</td>

                  <td className="py-2 px-2 flex justify-end gap-3">

                    <button
                      aria-label={`Eliminar a ${message.content}`}
                      title="Crear contacto"
                      className={`p-1 rounded-md text-primary hover:bg-secondary hover:text-white transition-colors flex items-center justify-center`}
                      onClick={() => setModalOpen(true)}
                    >
                      <UserRoundPlus size={24} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      
      {/* Modal para crear nuevo contacto */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Nuevo contacto"
      >
        <ContactForm
          onSubmit={(data) => {
            addContact(data);
            setModalOpen(false);
          }}
          onCancel={() => setModalOpen(false)}
        />
      </Modal>
    </>
  );
};