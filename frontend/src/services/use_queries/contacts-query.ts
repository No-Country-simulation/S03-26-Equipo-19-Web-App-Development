import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "../../store/useAuthStore";
import type { ContactResType, ContactsDashboardResType } from "../../types/contact.types";
import { getContactById, getContacts, getContactsDashboard } from "../use_cases/contacts-service";

export const useGetContacts = () => {
  const token = useAuthStore((state) => state.token);
  return useQuery<ContactResType[]>({
    queryKey: ["contacts"],
    queryFn: () => getContacts(token!),
    enabled: !!token,
  });
};

export const useGetContactsDashboard = () => {
  const token = useAuthStore((state) => state.token);
  return useQuery<ContactsDashboardResType>({
    queryKey: ["contactsDashboard"],
    queryFn: () => getContactsDashboard(token!),
    enabled: !!token,
  });
};


export const useGetContactById = (contactId: number) => {
  const token = useAuthStore((state) => state.token);
  return useQuery<ContactResType>({
    queryKey: ["contact", contactId],
    queryFn: () => getContactById(token!, contactId),
    enabled: !!token && !!contactId,
  });
};  