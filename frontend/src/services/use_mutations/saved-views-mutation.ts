import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { SavedViewRequest } from "../../types/admin.types";
import { createSavedView, deleteSavedView, updateSavedView } from "../use_cases/saved-views-service";

export const useSavedViewsMutations = () => {
  const qc = useQueryClient();

  const createSavedViewMutation = useMutation({
    mutationFn: (data: SavedViewRequest) => createSavedView(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["saved-views"] }),
  });

  const updateSavedViewMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: SavedViewRequest }) =>
      updateSavedView(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["saved-views"] }),
  });

  const removeSavedViewMutation = useMutation({
    mutationFn: (id: number) => deleteSavedView(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["saved-views"] }),
  });

 return {
  create: createSavedViewMutation,
  update: updateSavedViewMutation,
  remove: removeSavedViewMutation
};
};