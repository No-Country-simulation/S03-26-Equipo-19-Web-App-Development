import { useQuery } from "@tanstack/react-query";
import {
  getContactsMetrics,
  getGlobalMetrics,
  getMetricsByPeriod,
  getPanelMetrics,
  getTasksMetrics,
} from "../use_cases/metrics-service";
import { useAuthStore } from "../../store/useAuthStore";
import type { FunnelResType, GlobalMetricsResType, PanelResType, TasksMetricsResType } from "../../types/metric.types";


export const useGetMetricsByPeriod = (from: string, to: string) =>
  useQuery({
    queryKey: ["metrics-period", from, to],
    queryFn: () => getMetricsByPeriod(from, to),
    enabled: !!from && !!to,
  });

export const useGetTasksMetrics = () => {
  const token = useAuthStore((state) => state.token);

  return useQuery<TasksMetricsResType >({
    queryKey: ["metrics-tasks"],
    queryFn: () => getTasksMetrics(token!),
    enabled: !!token,
  });
};


export const useGetContactsMetrics = () => {
  const token = useAuthStore((state) => state.token);

  return useQuery<FunnelResType>({
    queryKey: ["metrics-contacts"],
    queryFn: () => getContactsMetrics(token!),
    enabled: !!token,
  });
};


export const useGetPanelAdminMetrics = () => {
  const token = useAuthStore((state) => state.token);

  return useQuery<PanelResType>({
    queryKey: ["metrics-panelAdmin"],
    queryFn: () => getPanelMetrics(token!),
    enabled: !!token,
  });
};

export const useGetGLobalMetrics = () => {
  const token = useAuthStore((state) => state.token);

  return useQuery<GlobalMetricsResType>({
    queryKey: ["metrics-global"],
    queryFn: () => getGlobalMetrics(token!),
    enabled: !!token,
  });
};