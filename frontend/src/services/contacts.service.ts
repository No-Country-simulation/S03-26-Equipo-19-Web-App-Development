import { api } from './api';
import type { Contact } from '../types/contact.types';

export const contactsService = {
  getAll: () => api.get<Contact[]>('/contacts'),
  getById: (id: string) => api.get<Contact>(`/contacts/${id}`),
  create: (data: Omit<Contact, 'id' | 'createdAt' | 'lastContactedAt'>) =>
    api.post<Contact>('/contacts', data),
  update: (id: string, data: Partial<Contact>) =>
    api.put<Contact>(`/contacts/${id}`, data),
  delete: (id: string) => api.delete<void>(`/contacts/${id}`),
};