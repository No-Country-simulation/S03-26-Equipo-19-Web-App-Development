import type { Channel } from './contact.types';

export type MessageDirection = 'INBOUND' | 'OUTBOUND';
export type MessageType = 'TEXT' | 'MEDIA';
export type MessageStatus = 'SENT' | 'DELIVERED' | 'READ' | 'FAILED';
export type InboundMessageStatus = "ENTRANTE" | "READ"


export interface MessageResType {
  id: number;
  userId: number;
  channel: Channel;
  direction: MessageDirection;
  body: string;
  createdAt: string;
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
status: 'OPEN' | 'CLOSED';
channel: Channel; 
createdAt: string;
updatedAt: string;
}

