import TitleSection from '../components/ui/TitleSection'
import { Button } from '../components/ui/Button'
import { ContactsTable } from '../components/dashboard/ContactsTable';
import { KpiCard } from '../components/ui/KpiCard';
import { FileUser, Speech, UsersRound, UserStar } from 'lucide-react';
import { Modal } from '../components/ui/Modal';
import { ContactForm } from '../components/contacts/ContactForm';
import { useMemo, useState } from 'react';
import type { ContactReqType } from '../types/contact.types';
import { ContactsMutationsService } from '../services/use_mutations/contacts-mutation';
import { useGetSavedViews } from '../services/use_queries/saved-views-query';
import { useGetContacts } from '../services/use_queries/contacts-query';
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from "../components/ui/select";
import { useGetContactsMetrics } from '../services/use_queries/metrics-query';


interface ContactsPageProps {
  isAdminView?: boolean;
}

export const ContactsPage = ({ isAdminView = false }: ContactsPageProps) => {

  const [modalOpen, setModalOpen] = useState(false);
  const [selectedViewName, setSelectedViewName] = useState<string | null>(null);

  const { mutationPostContact } = ContactsMutationsService();
  const { data: views = [], isLoading } = useGetSavedViews();
  const { data: contacts = [] } = useGetContacts();       // backend filtra por rol automáticamente
  const { data: metrics } = useGetContactsMetrics();

  const addContact = (data: ContactReqType) => {
    mutationPostContact.mutate(data, {
      onSuccess: () => setModalOpen(false)
    });
  };

  const contactViews = views.filter(view => view.entity === "CONTACTS");

  const selectedView = contactViews.find(
    (view) => view.name === selectedViewName
  );

  const filteredContacts = useMemo(() => {
    if (!selectedView) return contacts;

    const filters = selectedView.filters as {
      funnelStatus?: string;
      tagIds?: number[];
    };

    return contacts.filter((contact) => {
      if (
        filters.funnelStatus &&
        contact.funnelStatus !== filters.funnelStatus
      ) {
        return false;
      }

      if (filters.tagIds) {
        return filters.tagIds.some(tagId =>
          contact.tags?.some(t => t.id === tagId)
        );
      }

      return true;
    });
  }, [contacts, selectedView]);

  const contactsToShow = selectedView ? filteredContacts : contacts;

  return (
    <>
      <div className="flex justify-center md:justify-between mb-6">
        {/* Título dinámico según rol */}
        <TitleSection
          text={isAdminView ? "Todos los contactos" : "Mis contactos"}
          className='hidden md:flex'
        />
        {/* Ambos roles pueden crear contacto */}
        <Button
          variant='secondary'
          className="w-1/2 md:w-1/4 lg:w-1/6"
          onClick={() => setModalOpen(true)}
        >
          Nuevo contacto
        </Button>
      </div>
      {/* KPIs — igual para ambos */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <KpiCard title="Total de contactos"   metric={metrics?.funnel.total          ?? { value: 0, changePercent: 0, trend: 'stable' }} color="primary"    icon={<UsersRound size={28} />} />
        <KpiCard title="Contactos activos"    metric={metrics?.funnel.totalActive    ?? { value: 0, changePercent: 0, trend: 'stable' }} color="secondary"  icon={<Speech size={28} />} />
        <KpiCard title="Nuevos contactos"     metric={metrics?.funnel.byStatus.NEW_LEAD    ?? { value: 0, changePercent: 0, trend: 'stable' }} color="success"    icon={<UserStar size={28} />} />
        <KpiCard title="Contactos perdidos"   metric={metrics?.funnel.byStatus.CLOSED_LOST ?? { value: 0, changePercent: 0, trend: 'stable' }} color="error"      icon={<FileUser size={28} />} impact="negative" />
      </div>

      {/* Selector de vistas guardadas */}
      <div className="flex justify-center md:justify-end mb-6">
        <Select value={selectedViewName ?? ""} onValueChange={(v) => setSelectedViewName(v || null)}>
          <SelectTrigger className="w-1/2 md:w-1/4 lg:w-1/6">
            <SelectValue placeholder="Vistas guardadas" />
          </SelectTrigger>
          <SelectContent className="bg-white">
            <SelectItem value="">Sin selección</SelectItem>
            {contactViews.map((view) => (
              <SelectItem key={view.id} value={view.name}>{view.name}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

       <ContactsTable
        contacts={contactsToShow}
        isLoading={isLoading}
        isAdminView={isAdminView}
      />


      {/* Modal para crear nuevo contacto */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Nuevo contacto">
        <ContactForm
          onSubmit={(data) => { addContact(data); setModalOpen(false); }}
          onCancel={() => setModalOpen(false)}
        />
      </Modal>

    </>
  )
}


