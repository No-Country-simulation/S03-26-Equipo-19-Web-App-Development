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