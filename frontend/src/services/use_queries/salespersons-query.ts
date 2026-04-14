import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getSalespersons,
  getSalespersonById,
  createSalesperson,
  updateSalesperson,
  deleteSalesperson,
} from "../use_cases/salespersons-service";
import type { CreateSalespersonRequest, UpdateSalespersonRequest } from "../../types/admin.types";

// --- QUERIES ---
export const useGetSalespersons = (enabled: boolean) => {
  return useQuery({
    queryKey: ["salespersons"],
    queryFn: getSalespersons,
    enabled,
  });
};

export const useGetSalespersonById = (id: number) =>
  useQuery({
    queryKey: ["salesperson", id],
    queryFn: () => getSalespersonById(id),
    enabled: !!id,
  });

// --- MUTATIONS ---
export const useSalespersonsMutations = () => {
  const qc = useQueryClient();

  const create = useMutation({
    mutationFn: (data: CreateSalespersonRequest) => createSalesperson(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["salespersons"] }),
  });

  const update = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateSalespersonRequest }) =>
      updateSalesperson(id, data),
    onSuccess: (_, { id }) => {
      qc.invalidateQueries({ queryKey: ["salespersons"] });
      qc.invalidateQueries({ queryKey: ["salesperson", id] });
    },
  });

  const remove = useMutation({
    mutationFn: (id: number) => deleteSalesperson(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["salespersons"] }),
  });

  return { create, update, remove };
};