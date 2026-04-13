import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getTags, createTag, updateTag, deleteTag } from "../use_cases/tags-service";
import type { TagRequest } from "../../types/admin.types";

export const useGetTags = () =>
  useQuery({
    queryKey: ["tags"],
    queryFn: getTags,
  });

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