export type LeadStatus = "Lead Activo" | "En Seguimiento" | "Cliente Cerrado";

export type Channel = "WhatsApp" | "Email";

export interface Lead {
  id: string;
  name: string;
  email: string;
  status: LeadStatus;
  channel: Channel;
  timestamp: number;
}

export interface LeadFormData {
  name: string;
  email: string;
  status: LeadStatus;
  channel: Channel;
}

export interface User {
  uid: string;
  email: string | null;
  displayName: string | null;
  photoURL: string | null;
}

export interface ChartData {
  label: string;
  value: number;
}

export interface LineChartData {
  value: number;
}

export interface PieChartData {
  label: string;
  value: number;
  percent: number;
}

export interface StatCardData {
  title: string;
  value: string;
  change: string;
  trend: "up" | "down";
  icon: string;
  color: "blue" | "emerald" | "amber" | "purple";
}

export interface FunnelStage {
  label: string;
  value: number;
  color: string;
  icon: string;
}

export interface Stats {
  total: number;
  active: number;
  closed: number;
}
