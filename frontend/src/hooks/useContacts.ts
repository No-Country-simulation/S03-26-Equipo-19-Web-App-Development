import { useState, useEffect } from 'react';
import type { Contact } from '../types/contact.types';
// import { contactsService } from '../services/contacts.service';

// Datos mock mientras el backend no está listo
const MOCK_CONTACTS: Contact[] = [
  {
    id: '1',
    name: 'Ana García',
    email: 'ana@email.com',
    phone: '+54 351 123 4567',
    stage: 'active',
    channel: 'whatsapp',
    tags: ['startup', 'tech'],
    createdAt: '2024-01-15',
    lastContactedAt: '2024-03-10',
  },
  {
    id: '2',
    name: 'Carlos Méndez',
    email: 'carlos@email.com',
    phone: '+54 351 987 6543',
    stage: 'lead',
    channel: 'email',
    tags: ['enterprise'],
    createdAt: '2024-02-01',
    lastContactedAt: '2024-03-08',
  },
  {
    id: '3',
    name: 'Sofía Torres',
    email: 'sofia@email.com',
    phone: '+54 11 555 7890',
    stage: 'following',
    channel: 'whatsapp',
    tags: ['startup'],
    createdAt: '2024-02-20',
    lastContactedAt: '2024-03-12',
  },
  {
    id: '4',
    name: 'Martín López',
    email: 'martin@email.com',
    phone: '+54 11 444 1234',
    stage: 'closed',
    channel: 'email',
    tags: ['enterprise', 'tech'],
    createdAt: '2024-01-05',
    lastContactedAt: '2024-02-28',
  },
];

export const useContacts = () => {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchContacts = async () => {
      try {
        setLoading(true);
        // Cuando el backend esté listo, reemplazar por:
        // const data = await contactsService.getAll();
        await new Promise(res => setTimeout(res, 500)); // simula latencia
        setContacts(MOCK_CONTACTS);
      } catch (err) {
        setError('Error al cargar contactos');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchContacts();
  }, []);

  const addContact = async (data: Omit<Contact, 'id' | 'createdAt' | 'lastContactedAt'>) => {
    try {
      // const newContact = await contactsService.create(data);
      const newContact: Contact = {
        ...data,
        id: crypto.randomUUID(),
        createdAt: new Date().toISOString(),
        lastContactedAt: new Date().toISOString(),
      };
      setContacts(prev => [newContact, ...prev]);
    } catch (err) {
      console.error(err);
    }
  };

  const deleteContact = async (id: string) => {
    try {
      // await contactsService.delete(id);
      setContacts(prev => prev.filter(c => c.id !== id));
    } catch (err) {
      console.error(err);
    }
  };

  return { contacts, loading, error, addContact, deleteContact };
};