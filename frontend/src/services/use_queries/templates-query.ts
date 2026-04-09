import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "../../store/useAuthStore";
import type { TemplateResType } from "../../types/template.types";
import { getTemplates } from "../use_cases/templates-service";

export const useGetTemplates = () => {
  const token = useAuthStore((state) => state.token);
  return useQuery<TemplateResType[]>({
    queryKey: ["templates"],
    queryFn: () => getTemplates(token!),
    enabled: !!token,
  });
};