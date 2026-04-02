import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "../../store/useAuthStore";
import type { ContactResType } from "../../types/contact.types";
import { getContacts } from "../use_cases/contacts-service";

export const useGetContacts = () => {
  const token = useAuthStore((state) => state.token);
  return useQuery<ContactResType[]>({
    queryKey: ["contacts"],
    queryFn: () => getContacts(token!),
    enabled: !!token,
  });
};

