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

//  Contactos del vendedor logueado
/* export const useGetContactsBySalesperson = (id?: number) => {
  const token = useAuthStore((state) => state.token);
 
  return useQuery<{ contact: ContactResType }[]>({
    queryKey: ["contacts", id],
    queryFn: () => getContacts(token!), 
    enabled: !!token && !!id,
    select: (data) =>
      data.filter((item) => item.contact.owner.id === id),
  });
}; */