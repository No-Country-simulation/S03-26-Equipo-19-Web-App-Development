export type FunnelStage = 'lead' | 'active' | 'following' | 'closed';

export type Channel = 'whatsapp' | 'email';

export interface Contact {
  id: string;
  name: string;
  email: string;
  phone: string;
  stage: FunnelStage;
  channel: Channel;
  tags: string[];
  createdAt: string;
  lastContactedAt: string;
}