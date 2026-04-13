import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getFunnelStages,
  createFunnelStage,
  updateFunnelStage,
  deleteFunnelStage,
} from "../use_cases/funnels-service";
import type { FunnelStageRequest } from "../../types/admin.types";

export const useGetFunnelStages = () =>
  useQuery({
    queryKey: ["funnel-stages"],
    queryFn: getFunnelStages,
  });

export const useFunnelMutations = () => {
  const qc = useQueryClient();

  const create = useMutation({
    mutationFn: (data: FunnelStageRequest) => createFunnelStage(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["funnel-stages"] }),
  });

  const update = useMutation({
    mutationFn: ({ id, data }: { id: number; data: FunnelStageRequest }) =>
      updateFunnelStage(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["funnel-stages"] }),
  });

  const remove = useMutation({
    mutationFn: (id: number) => deleteFunnelStage(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["funnel-stages"] }),
  });

  return { create, update, remove };
};