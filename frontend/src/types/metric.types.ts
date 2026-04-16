import type { Channel } from "./contact.types";

export interface KpiData {
  title: string;
  value: string;
  trend: string;
  isPositive: boolean;
}

export interface ChartDataPoint {
  label: string;
  value: number;
}

export interface MetricsSummary {
  activeContacts: KpiData;
  messagesSent: KpiData;
  responseRate: KpiData;
  growth: ChartDataPoint[];
  byChannel: ChartDataPoint[];
}

// valores comunes
export type Trend = "up" | "down" | "stable";

export interface Metric {
  value: number;
  changePercent: number;
  trend: Trend;
}

// funnel
export type FunnelStatus =
  | "NEW_LEAD"
  | "CONTACTED"
  | "IN_NEGOTIATION"
  | "PROPOSAL_SENT"
  | "CLOSED_WON"
  | "CLOSED_LOST";

export type FunnelByStatus = Record<FunnelStatus, Metric>;

export interface FunnelResType {
  funnel: {
    byStatus: FunnelByStatus;
    totalActive: Metric;
    total: Metric;
  };
}

export type MessagesByChannel = Record<Channel, Metric>;

export interface MessagesResType {
  messages: {
    sent: Metric;
    received: Metric;
    responseRate: Metric;
    byChannel: MessagesByChannel;
  };
}

// tasks
export interface TasksMetricsResType {
  tasks: {
    completed: Metric;
    overdue: Metric;
    pending: Metric;
    dueToday: Metric;
    total: Metric;
  };
}

// users
export interface UsersResType {
  users: {
    total: Metric;
    active: Metric;
    inactive: Metric;
    newUsers: Metric;
  };
}

export interface PanelResType {
  totalContacts: Metric;
  totalMessages: Metric;
  uncomingTasks: Metric;
}

export type TopSalesPerson = {
    id: number;
    name: string;
    email: string;
    messagesSent: number;
    performanceScore: number; 
}

export interface GlobalMetricsResType {
  totalConversations: Metric;
  responseRate: Metric;
  completedTasks: Metric;
  topSalesperson: TopSalesPerson
}
