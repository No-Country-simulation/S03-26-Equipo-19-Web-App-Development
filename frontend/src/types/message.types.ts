import type { Channel } from './contact.types';

export type MessageDirection = 'inbound' | 'outbound';
export type MessageType = 'text' | 'media';
export type MessageStatus = 'sent' | 'delivered' | 'read' | 'failed';


export interface MessageResType {
  id: number;
  userId: number;
  channel: Channel;
  direction: MessageDirection;
  content: string;
  createdAt: Date;
  externalId: string;
  messageType: MessageType;
  fileUrl?: string;
  status: MessageStatus;
  conversationId: number;
}

export interface Conversation {
id: number;
contactId: number;
userId: number;
status: 'open' | 'closed';
channel: Channel; 
createdAt: Date;
updatedAt: Date;
}