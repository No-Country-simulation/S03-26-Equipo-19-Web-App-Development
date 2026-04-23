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
