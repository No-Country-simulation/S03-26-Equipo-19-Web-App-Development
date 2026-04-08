import type { Channel } from "./contact.types";

type TemplateAuthor = {
      id: number;
        name: string;
        email: string;
        passwordHash: string;
        role: 'ADMIN' | 'USER';
        active: boolean;
        createdAt: Date;
        updatedAt: Date;
    }

export interface TemplateResType {
    id: number;
    name: string;
    channel: Channel;
    subject?: string;
    body: string;   
    variables: string; // JSON string con las variables disponibles
    createdBy: TemplateAuthor;
    createdAt: Date;
    updatedAt: Date;
}
