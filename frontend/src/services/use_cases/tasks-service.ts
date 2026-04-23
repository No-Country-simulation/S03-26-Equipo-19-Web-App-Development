import { useAuthStore } from "../../store/useAuthStore";
import type { TaskReqType, TaskResType } from "../../types/task.types";
import { apiTasksService } from "../general_api";

export const getTasks = async (token: string): Promise<TaskResType[]> => {
  if (!token) throw new Error("No hay token de autenticación.");
  try {
    const res = await apiTasksService.get("", {
      headers: { Authorization: `Bearer ${token}` },
    });
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error(
      (error as { response?: { data?: { message?: string } } }).response?.data
        ?.message ?? msg,
    );
  }
};

export const getTaskByContactId = async (
  contactId: number,
): Promise<TaskResType[]> => {
  const { token } = useAuthStore.getState();
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiTasksService.get(`/contact/${contactId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error(
      (error as { response?: { data?: { message?: string } } }).response?.data
        ?.message ?? msg,
    );
  }
};

export const createTask = async (data: TaskReqType): Promise<TaskResType> => {
  const { token } = useAuthStore.getState();
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiTasksService.post("", data, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error(
      (error as { response?: { data?: { message?: string } } }).response?.data
        ?.message ?? msg,
    );
  }
};

export const updateTaskById = async (
  taskId: number,
  data: TaskReqType,
): Promise<TaskResType> => {
  const { token } = useAuthStore.getState();
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiTasksService.put(`/${taskId}`, data, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error(
      (error as { response?: { data?: { message?: string } } }).response?.data
        ?.message ?? msg,
    );
  }
};

export const updateTaskStatusById = async (
  taskId: number,
): Promise<TaskResType> => {
  const { token } = useAuthStore.getState();
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiTasksService.patch(`/${taskId}/complete`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error(
      (error as { response?: { data?: { message?: string } } }).response?.data
        ?.message ?? msg,
    );
  }
};

export const deleteTaskById = async (taskId: number): Promise<TaskResType> => {
  const { token } = useAuthStore.getState();
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiTasksService.delete(`/${taskId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error(
      (error as { response?: { data?: { message?: string } } }).response?.data
        ?.message ?? msg,
    );
  }
};