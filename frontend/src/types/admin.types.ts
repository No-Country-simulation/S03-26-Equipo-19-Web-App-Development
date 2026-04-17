// src/types/admin.types.ts

// ─── SALESPERSONS ─────────────────────────────────────────────────────────────
export interface SalespersonResponse {
  id: number;
  name: string;
  email: string;
  status: 'ACTIVE' | 'INACTIVE';
  assignedContacts: number;
  messagesSent: number;
  responseRate: number;
  lastActivity: string;
}

export interface CreateSalespersonRequest {
  name: string;
  email: string;
  password: string;
}

export interface UpdateSalespersonRequest {
  name?: string;
  email?: string;
  status?: 'ACTIVE' | 'INACTIVE';
}

// ─── TAGS ─────────────────────────────────────────────────────────────────────
export interface TagResponse {
  id: number;
  name: string;
  color: string;
}

export interface TagRequest {
  name: string;
  color: string;
}

// ─── EXPORT ───────────────────────────────────────────────────────────────────
export type ExportFormat = 'PDF' | 'CSV';
export type ExportEntity =
  | 'contacts'
  | 'conversations'
  | 'messages'
  | 'tasks';

export interface ExportRequest {
  format: ExportFormat;
  entityType: ExportEntity; 
  filters?: Record<string, unknown>;
}

// ─── SAVED VIEWS ──────────────────────────────────────────────────────────────
export interface SavedViewResponse {
  id: number;
  name: string;
  entity: 'CONTACTS' | 'TASKS';
  isDefault?: boolean;
  isGlobal?: boolean;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
  filters: Record<string, unknown>;
  creator: string;
}

export interface SavedViewRequest {
  name: string;
  entity: 'CONTACTS' | 'TASKS';
  isDefault?: boolean;
  global: boolean;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
  filters: Record<string, unknown>;
}

// ─── METRICS ──────────────────────────────────────────────────────────────────
export interface PeriodMetrics {
  date: string;
  inbound: number;
  outbound: number;
}

export interface AgentMetrics {
  id: number;
  name: string;
  status: "active" | "inactive";
  messagesSent: number;
  responseRate: number;
}

// ─── FUNNEL ───────────────────────────────────────────────────────────────────
export interface FunnelStageResponse {
  id: number;
  name: string;
  order: number;
  status: "ACTIVE" | "INACTIVE";
}

export interface FunnelStageRequest {
  name: string;
  order: number;
}