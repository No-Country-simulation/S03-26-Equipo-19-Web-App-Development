import { api } from './api';
import type { Conversation, Message } from '../types/message.types';

export const messagesService = {
  getConversations: () => api.get<Conversation[]>('/conversations'),
  getByContact: (contactId: string) =>
    api.get<Message[]>(`/conversations/${contactId}/messages`),
  send: (contactId: string, content: string, channel: string) =>
    api.post<Message>(`/conversations/${contactId}/messages`, { content, channel }),
};