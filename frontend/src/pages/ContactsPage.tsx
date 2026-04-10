import TitleSection from '../components/ui/TitleSection'
import { Button } from '../components/ui/Button'
import { ContactsTable } from '../components/dashboard/ContactsTable';
import { KpiCard } from '../components/ui/KpiCard';
import { FileUser, Speech, UsersRound, UserStar } from 'lucide-react';
import { Modal } from '../components/ui/Modal';
import { ContactForm } from '../components/contacts/ContactForm';
import { useState } from 'react';
import type { ContactReqType } from '../types/contact.types';
import { ContactsMutationsService } from '../services/use_mutations/contacts-mutation';



export const ContactsPage = () => {

  const [modalOpen, setModalOpen] = useState(false);

  const { mutationPostContact } = ContactsMutationsService();

  const addContact = (data: ContactReqType) => {
    mutationPostContact.mutate(data, {
      onSuccess: () => {
        setModalOpen(false);
      }
    });
  };


  return (
    <>
      <div className="flex justify-center md:justify-between mb-6">
        <TitleSection text="Mis contactos" className='hidden md:flex' />
        <Button variant='secondary' className="w-1/2 md:w-1/4 lg:w-1/6" onClick={() => setModalOpen(true)}>
          Nuevo contacto
        </Button>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <KpiCard title="Total de contactos" value="15" trend="8.5%" color="primary" isPositive={true} icon={<UsersRound size={28} />} />
        <KpiCard title="Contactos activos" value="09" trend="2.1%" color="secondary" isPositive={false} icon={<Speech size={28} />} />
        <KpiCard title="Nuevos contactos" value="125" trend="4.3%" color="success" isPositive={true} icon={<UserStar size={28} />} />
        <KpiCard title="Contactos perdidos" value="23" trend="4.3%" color="error" isPositive={true} icon={<FileUser size={28} />} />
      </div>

      <div className="flex justify-center md:justify-end mb-6">
        <select className="w-1/2 md:w-1/4 lg:w-1/6 p-2 border border-neutro-2 rounded-lg focus:outline-none focus:ring-1 focus:ring-accent">
          <option value="">Vistas guardadas</option>
          <option value="vista1">Vista 1</option>
          <option value="vista2">Vista 2</option>
          <option value="vista3">Vista 3</option>
        </select>
      </div>

      <div className=""><ContactsTable /></div>

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
  )
}


