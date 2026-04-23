import { apiMetricsService } from "../general_api";
import type { PeriodMetrics } from "../../types/admin.types";


export type MetricsParams = {
  startDate?: string;
  endDate?: string;
};


// MÉTRICAS TAREAS
export const getTasksMetrics = async (token: string) => {
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiMetricsService.get("/tasks", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
  }
};

// MÉTRICAS CONTACTOS
export const getContactsMetrics = async (token: string) => {
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiMetricsService.get("/contacts", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
  }
};

// MÉTRICAS PANEL ADMIN
export const getPanelMetrics = async (token: string) => {
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiMetricsService.get("/panel", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
  }
};

// MÉTRICAS GLOBALES
export const getGlobalMetrics = async (token: string) => {
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiMetricsService.get("/global-metrics", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
  }
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