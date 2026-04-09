export type FunnelStage =
  | "NEW_LEAD"
  | "CONTACTED"
  | "IN_NEGOTIATION"
  | "PROPOSAL_SENT"
  | "CLOSED_WON"
  | "CLOSED_LOST";

export type Channel = "WHATSAPP" | "EMAIL";

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
  createdAt: Date;
  updatedAt: Date;
}

export interface ContactReqType {
  contact: {
    name: string;
    lastName: string;
    email: string;
    phone: string;
    company: string;
  };
  funnelStatus: FunnelStage;
  preferredChannel: Channel;
  ownerId: number;
  tags: Tag[];
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
