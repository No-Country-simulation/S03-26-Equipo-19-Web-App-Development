import type { Channel } from './contact.types';

export type MessageDirection = 'inbound' | 'outbound';

export interface Message {
  id: string;
  contactId: string;
  channel: Channel;
  direction: MessageDirection;
  content: string;
  sentAt: string;
  read: boolean;
}

export interface Conversation {
  contactId: string;
  contactName: string;
  channel: Channel;
  lastMessage: string;
  lastMessageAt: string;
  unreadCount: number;
  messages: Message[];
}