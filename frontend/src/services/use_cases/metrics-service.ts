import { apiMetricsService } from "../general_api";
import type { DashboardMetrics, PeriodMetrics } from "../../types/admin.types";

export const getMetricsDashboard = async (): Promise<DashboardMetrics> => {
  const res = await apiMetricsService.get("/dashboard");
  return res.data;
};

export const getMetricsByPeriod = async (
  from: string,
  to: string
): Promise<PeriodMetrics[]> => {
  const res = await apiMetricsService.get("/period", {
    params: { from, to },
  });
  return res.data;
};

export const exportMetrics = async (): Promise<Blob> => {
  const res = await apiMetricsService.get("/export", {
    responseType: "blob",
  });
  return res.data;
};