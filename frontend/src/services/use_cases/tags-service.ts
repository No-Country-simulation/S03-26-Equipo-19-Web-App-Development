import { apiTagsService } from "../general_api";
import type { TagResponse, TagRequest } from "../../types/admin.types";

export const getTags = async (): Promise<TagResponse[]> => {
  const res = await apiTagsService.get("");
  return res.data;
};

export const createTag = async (data: TagRequest): Promise<TagResponse> => {
  const res = await apiTagsService.post("", data);
  return res.data;
};

export const updateTag = async (id: number, data: TagRequest): Promise<TagResponse> => {
  const res = await apiTagsService.put(`/${id}`, data);
  return res.data;
};

export const deleteTag = async (id: number): Promise<void> => {
  await apiTagsService.delete(`/${id}`);
};