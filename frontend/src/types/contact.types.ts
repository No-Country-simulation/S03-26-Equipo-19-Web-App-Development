import type { ConversationResType } from "./conversation.types";

export type FunnelStage =
  | "NEW_LEAD"
  | "CONTACTED"
  | "IN_NEGOTIATION"
  | "PROPOSAL_SENT"
  | "CLOSED_WON"
  | "CLOSED_LOST";

export type Channel = "WHATSAPP" | "EMAIL";

export interface Metrics {
  funnel: {
    byStatus: {
      NEW_LEAD: number;
      CONTACTED: number;
    };
    totalActive: number;
  };
  messages: {
    sent: number;
    received: number;
    responseRate: number;
    byChannel: {
      WHATSAPP: number;
      EMAIL: number;
    };
  };
  tasks: {
    completed: number;
    overdue: number;
    pending: number;
  };
  period: string;
  salespersonEmail: string;
}

export interface ContactResType {
  id: number;
  name: string;
  lastName: string;
  email: string;
  phone: string;
  company: string;
  funnelStatus: FunnelStage;
  source: string;
  preferredChannel: Channel;
  owner: Owner;
  tags: Tag[];
  conversations?: ConversationResType[];
  createdAt: string;
  updatedAt?: string;
  totalUnreadCount?: number;
}

export interface ContactsDashboardResType {
  metrics: Metrics;
  contacts: ContactResType[];
}

export interface ContactReqType {
  contact: {
    name: string;
    lastName: string;
    email?: string;
    phone?: string;
    company?: string;
  };
  preferredChannel: Channel;
  ownerId?: number;
}

export interface Owner {
  id: number;
  name: string;
  email: string;
  passwordHash: string;
  role: "ADMIN" | "SALESPERSON";
  active: true;
  createdAt: Date;
  updatedAt: Date;
}

export interface Tag {
  id: number;
  name: string;
  color: string;
  description: string;
  createdAt: Date;
}
