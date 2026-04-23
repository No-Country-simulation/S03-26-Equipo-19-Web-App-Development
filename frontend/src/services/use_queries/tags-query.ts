import { useQuery } from "@tanstack/react-query";
import { getTags } from "../use_cases/tags-service";

export const useGetTags = () =>
  useQuery({
    queryKey: ["tags"],
    queryFn: getTags,
  });

