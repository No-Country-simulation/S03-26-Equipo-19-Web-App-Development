import { create } from 'zustand';
import type { ContactReqType } from '../types/contact.types';

type Contact = ContactReqType & {
  id: string;
};

interface ContactsState {
  contacts: Contact[];
  addContact: (contact: ContactReqType) => void;
}

export const useContactsStore = create<ContactsState>((set) => ({
  contacts: [],

  addContact: (contact) =>
    set((state) => ({
      contacts: [
        ...state.contacts,
        {
          ...contact,
          id: crypto.randomUUID(),
        },
      ],
    })),
}));