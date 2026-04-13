import { apiContactsService, apiMetricsService } from "../general_api";
import type { DashboardMetrics } from "../../types/admin.types";

export const getContactsDashboard = async () => {
  const res = await apiContactsService.get("/dashboard");
  return res.data;
};

export const getAdminDashboard = async (): Promise<DashboardMetrics> => {
  const res = await apiMetricsService.get("/dashboard");
  return res.data;
};