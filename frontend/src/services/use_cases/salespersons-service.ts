import { apiSalespersonService } from "../general_api";
import type {
  SalespersonResponse,
  CreateSalespersonRequest,
  UpdateSalespersonRequest,
} from "../../types/admin.types";

export const getSalespersons = async (): Promise<SalespersonResponse[]> => {
  const res = await apiSalespersonService.get("");
  return res.data;
};

export const getSalespersonById = async (id: number): Promise<SalespersonResponse> => {
  const res = await apiSalespersonService.get(`/${id}`);
  return res.data;
};

export const createSalesperson = async (data: CreateSalespersonRequest): Promise<SalespersonResponse> => {
  const res = await apiSalespersonService.post("", data);
  return res.data;
};

export const updateSalesperson = async (id: number, data: UpdateSalespersonRequest): Promise<SalespersonResponse> => {
  const res = await apiSalespersonService.put(`/${id}`, data);
  return res.data;
};

export const deleteSalesperson = async (id: number): Promise<void> => {
  await apiSalespersonService.delete(`/${id}`);
};