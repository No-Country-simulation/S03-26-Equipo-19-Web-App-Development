import { useState, useMemo } from 'react';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { ContactFilters } from '../components/contacts/ContactFilters';
import { ContactForm } from '../components/contacts/ContactForm';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { useContacts } from '../hooks/useContacts';
import type { FunnelStage, Channel } from '../types/contact.types';

const stageLabel: Record<FunnelStage, string> = {
  lead: 'Lead',
  active: 'Activo',
  following: 'En seguimiento',
  closed: 'Cerrado',
};

export const ContactsPage = () => {
  const { contacts, loading, error, addContact, deleteContact } = useContacts();
  const [search, setSearch] = useState('');
  const [stage, setStage] = useState<FunnelStage | 'all'>('all');
  const [channel, setChannel] = useState<Channel | 'all'>('all');
  const [modalOpen, setModalOpen] = useState(false);

  const filtered = useMemo(() => {
    return contacts.filter(c => {
      const matchSearch = c.name.toLowerCase().includes(search.toLowerCase()) ||
                          c.email.toLowerCase().includes(search.toLowerCase());
      const matchStage = stage === 'all' || c.stage === stage;
      const matchChannel = channel === 'all' || c.channel === channel;
      return matchSearch && matchStage && matchChannel;
    });
  }, [contacts, search, stage, channel]);

  return (
    <DashboardLayout>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-white text-2xl font-bold">Contactos</h1>
        <Button onClick={() => setModalOpen(true)}>+ Nuevo contacto</Button>
      </div>

      <ContactFilters
        search={search}
        stage={stage}
        channel={channel}
        onSearchChange={setSearch}
        onStageChange={setStage}
        onChannelChange={setChannel}
      />

      {loading && <p className="text-gray-400 text-sm">Cargando contactos...</p>}
      {error && <p className="text-red-400 text-sm">{error}</p>}

      {!loading && !error && (
        <div className="bg-gray-900 border border-gray-700 rounded-2xl overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-700 text-gray-400 text-left">
                <th className="px-6 py-4 font-medium">Nombre</th>
                <th className="px-6 py-4 font-medium">Email</th>
                <th className="px-6 py-4 font-medium">Canal</th>
                <th className="px-6 py-4 font-medium">Estado</th>
                <th className="px-6 py-4 font-medium">Último contacto</th>
                <th className="px-6 py-4 font-medium"></th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-10 text-center text-gray-500">
                    No se encontraron contactos
                  </td>
                </tr>
              ) : (
                filtered.map(contact => (
                  <tr key={contact.id} className="border-b border-gray-800 hover:bg-gray-800/50 transition-colors">
                    <td className="px-6 py-4 text-white font-medium">{contact.name}</td>
                    <td className="px-6 py-4 text-gray-400">{contact.email}</td>
                    <td className="px-6 py-4 text-gray-400 capitalize">{contact.channel}</td>
                    <td className="px-6 py-4">
                      <Badge label={stageLabel[contact.stage]} variant={contact.stage} />
                    </td>
                    <td className="px-6 py-4 text-gray-400">{contact.lastContactedAt}</td>
                    <td className="px-6 py-4">
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => deleteContact(contact.id)}
                      >
                        Eliminar
                      </Button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

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
    </DashboardLayout>
  );
};