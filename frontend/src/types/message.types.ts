import type { Channel } from './contact.types';

export type MessageDirection = 'INBOUND' | 'OUTBOUND';
export type MessageStatus = 'SENT' | 'DELIVERED' | 'READ' | 'FAILED';
export type InboundMessageStatus = "ENTRANTE" | "READ"


export interface MessageResType {
  id: number;
  userId: number;
  direction: MessageDirection;
  body: string;
  deliveryStatus: MessageStatus;
  conversation: {
    id: number;
    channel: Channel;
  };
  providerId: string;
  sender: string | null;
  template: string | null;
  sentAt: string;
}


export interface MessageReqType {
  contactId: number,
  channel: Channel,
  content: {
    body: string
  }
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

