export type FunnelStage = "lead" | "active" | "following" | "closed";

export type Channel = "whatsapp" | "email";


export interface ContactResType {
  id: number;
  name: string;
  lastName: string;
  email: string;
  phone: string;
  company: string;
  funnelStatus: string;
  source: string;
  preferredChannel: "WHATSAPP";
  owner: Owner;
  tags: Tag[];
  createdAt: Date;
  updatedAt: Date;
}

export interface Owner {
  id: number;
  name: string;
  email: string;
  passwordHash: string;
  role: "ADMIN";
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
