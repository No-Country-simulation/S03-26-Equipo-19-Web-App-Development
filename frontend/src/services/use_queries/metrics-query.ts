import { useQuery } from "@tanstack/react-query";
import { getAdminDashboard, getContactsDashboard } from "../use_cases/dashboard-service";
import { getMetricsByPeriod } from "../use_cases/metrics-service";

export const useGetAdminDashboard = () =>
  useQuery({
    queryKey: ["admin-dashboard"],
    queryFn: getAdminDashboard,
  });

export const useGetContactsDashboard = () =>
  useQuery({
    queryKey: ["contacts-dashboard"],
    queryFn: getContactsDashboard,
  });

export const useGetMetricsByPeriod = (from: string, to: string) =>
  useQuery({
    queryKey: ["metrics-period", from, to],
    queryFn: () => getMetricsByPeriod(from, to),
    enabled: !!from && !!to,
  });