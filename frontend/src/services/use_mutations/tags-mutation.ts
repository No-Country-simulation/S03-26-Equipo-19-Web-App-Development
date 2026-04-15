import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { TagRequest } from "../../types/admin.types";
import { createTag, deleteTag, updateTag } from "../use_cases/tags-service";

export const useTagsMutations = () => {
  const qc = useQueryClient();

  const create = useMutation({
    mutationFn: (data: TagRequest) => createTag(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["tags"] }),
  });

  const update = useMutation({
    mutationFn: ({ id, data }: { id: number; data: TagRequest }) => updateTag(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["tags"] }),
  });

  const remove = useMutation({
    mutationFn: (id: number) => deleteTag(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["tags"] }),
  });

  return { create, update, remove };
};