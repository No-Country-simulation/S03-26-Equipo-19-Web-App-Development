import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "../../store/useAuthStore";
import type { TaskResType } from "../../types/task.types";
import { getTaskByContactId, getTasks } from "../use_cases/tasks-service";

export const useGetTasks = () => {
  const token = useAuthStore((state) => state.token);
  return useQuery<TaskResType[]>({
    queryKey: ["tasks"],
    queryFn: () => getTasks(token!),
    enabled: !!token,
  });
};

export const useGetTaskByContactId = (contactId: number) => {
  const token = useAuthStore((state) => state.token);
  return useQuery<TaskResType[]>({  
    queryKey: ["tasks", contactId],
    queryFn: () => getTaskByContactId(contactId),
    enabled: !!token && !!contactId,
  });
};