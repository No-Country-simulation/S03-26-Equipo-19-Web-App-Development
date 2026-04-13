import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getSavedViews,
  createSavedView,
  updateSavedView,
  deleteSavedView,
} from "../use_cases/saved-views-service";
import type { SavedViewRequest } from "../../types/admin.types";

export const useGetSavedViews = () =>
  useQuery({
    queryKey: ["saved-views"],
    queryFn: getSavedViews,
  });

export const useSavedViewsMutations = () => {
  const qc = useQueryClient();

  const create = useMutation({
    mutationFn: (data: SavedViewRequest) => createSavedView(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["saved-views"] }),
  });

  const update = useMutation({
    mutationFn: ({ id, data }: { id: number; data: SavedViewRequest }) =>
      updateSavedView(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["saved-views"] }),
  });

  const remove = useMutation({
    mutationFn: (id: number) => deleteSavedView(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["saved-views"] }),
  });

  return { create, update, remove };
};