import type { ContactResType } from "./contact.types";
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
