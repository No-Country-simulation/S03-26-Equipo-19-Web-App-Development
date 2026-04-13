import { apiContactsService } from "../general_api";
import type { FunnelStageResponse, FunnelStageRequest } from "../../types/admin.types";

// El backend expone las etapas del funnel a través de contacts/funnel-stages
// Ajustá la URL si el backend expone un endpoint dedicado
export const getFunnelStages = async (): Promise<FunnelStageResponse[]> => {
  const res = await apiContactsService.get("/funnel-stages");
  return res.data;
};

export const createFunnelStage = async (data: FunnelStageRequest): Promise<FunnelStageResponse> => {
  const res = await apiContactsService.post("/funnel-stages", data);
  return res.data;
};

export const updateFunnelStage = async (id: number, data: FunnelStageRequest): Promise<FunnelStageResponse> => {
  const res = await apiContactsService.put(`/funnel-stages/${id}`, data);
  return res.data;
};

export const deleteFunnelStage = async (id: number): Promise<void> => {
  await apiContactsService.delete(`/funnel-stages/${id}`);
};