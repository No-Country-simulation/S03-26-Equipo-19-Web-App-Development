import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  createTask,
  deleteTaskById,
  updateTaskById,
  updateTaskStatusById,
} from "../use_cases/tasks-service";
import type { TaskReqType } from "../../types/task.types";

export const useTasksMutationsService = () => {
  const queryClient = useQueryClient();

  const mutationPostTask = useMutation({
    mutationFn: (data: TaskReqType) => createTask(data),

    onMutate: async (newTask) => {
      await queryClient.cancelQueries({ queryKey: ["tasks"] });

      const previousTasks = queryClient.getQueryData(["tasks"]);

      queryClient.setQueryData(["tasks"], (old: any) => [
        ...(old || []),
        {
          ...newTask,
          id: Math.random(),
          status: "PENDING",
        },
      ]);
      return { previousTasks };
    },
    onError: (_err, _newTask, context) => {
      queryClient.setQueryData(["tasks"], context?.previousTasks);
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["tasks"] });
    },
  });

  const mutationUpdateTaskById = useMutation({
    mutationFn: ({ id, data }: { id: number; data: TaskReqType }) =>
      updateTaskById(id, data),

    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["tasks"] });
      queryClient.invalidateQueries({ queryKey: ["tasks", variables.id] });
    },
  });

  const mutationUpdateTaskStatusById = useMutation({
    mutationFn: ({ id }: { id: number }) => updateTaskStatusById(id),

    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["tasks"] });
      queryClient.invalidateQueries({ queryKey: ["tasks", variables.id] });
    },
  });

    const mutationDeleteTaskById = useMutation({
    mutationFn: ({ id }: { id: number }) => deleteTaskById(id),

    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["tasks"] });
      queryClient.invalidateQueries({ queryKey: ["tasks", variables.id] });
    },
  });


  return {
    mutationPostTask,
    mutationUpdateTaskById,
    mutationUpdateTaskStatusById,
    mutationDeleteTaskById
  };
};
