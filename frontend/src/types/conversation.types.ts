import type { Channel, ContactResType } from "./contact.types";
import type { Author } from "./template.types";

export interface ConversationResType {
  id: number;
  contact: ContactResType;
  channel: string;
  status: string;
  assignedTo: Author;
  lastInteraction: string;
  createdAt: string;
  updatedAt: string;
  unreadCount?: number
}

export interface ConversationInboxItemType {
 contactId: number;
conversationId: number;
lastMessageId: number;
channel: Channel;
lastMessagePreview: string;
contactIdentifier: string;
contactName: string;
lastMessageAt: string;
}