import { apiSavedViewsService } from "../general_api";
import type { SavedViewResponse, SavedViewRequest } from "../../types/admin.types";

export const getSavedViews = async (): Promise<SavedViewResponse[]> => {
  const res = await apiSavedViewsService.get("");
  return res.data;
};

export const getSavedViewById = async (id: number): Promise<SavedViewResponse> => {
  const res = await apiSavedViewsService.get(`/${id}`);
  return res.data;
};

export const createSavedView = async (data: SavedViewRequest): Promise<SavedViewResponse> => {
  const res = await apiSavedViewsService.post("", data);
  return res.data;
};

export const updateSavedView = async (id: number, data: SavedViewRequest): Promise<SavedViewResponse> => {
  const res = await apiSavedViewsService.put(`/${id}`, data);
  return res.data;
};

export const deleteSavedView = async (id: number): Promise<void> => {
  await apiSavedViewsService.delete(`/${id}`);
};