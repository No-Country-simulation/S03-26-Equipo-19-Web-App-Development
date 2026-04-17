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
export type ExportFormat = 'CSV' | 'PDF';
export type ExportEntity =
  | 'CONTACTS'
  | 'USERS'
  | 'TASKS'
  | 'SALESPERSONS'
  | 'CONVERSATIONS';

export interface ExportRequest {
  format: ExportFormat;
  entityType: ExportEntity;   // antes se llamaba "entity"
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



































































// ---- SALESPERSONS ----
export interface SalespersonResponse {
  id: number;
  name: string;
  email: string;
  status: "ACTIVE" | "INACTIVE";
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
  status?: "ACTIVE" | "INACTIVE";
}

// ---- TAGS ----
export interface TagResponse {
  id: number;
  name: string;
  color: string;
}

export interface TagRequest {
  name: string;
  color: string;
}

// ---- METRICS ----


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

type TaskFilters = {
  status?: string;
  dueDateFrom?: string;
  dueDateTo?: string;
};


export interface SavedViewResponse {
  id: number;
  name: string;
  entity: "CONTACTS" | "TASKS";
  isDefault?: boolean;
  isGlobal?: boolean;
  filters: TaskFilters;
/*   filters: Record<string, unknown>; */
  sortBy?: string;
  sortOrder?: "ASC" | "DESC";
  creator: string;
}

// ---- FUNNEL ----
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