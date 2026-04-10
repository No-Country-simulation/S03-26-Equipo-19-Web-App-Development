import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  postContact,
  updateContactById,
  updateFunnelStatusByContactId,
} from "../use_cases/contacts-service";
import type { ContactReqType } from "../../types/contact.types";

export const ContactsMutationsService = () => {
  const queryClient = useQueryClient();

  const mutationPostContact = useMutation({
    mutationFn: (data: ContactReqType) => postContact(data),

    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["contacts"] });
    },
  });

  const mutationUpdateContactById = useMutation({
    mutationFn: ({ id, data }: { id: number; data: ContactReqType }) =>
      updateContactById(id, data),

    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["contacts"] });
      queryClient.invalidateQueries({ queryKey: ["contact", variables.id] });
    },
  });

  const mutationUpdateFunnelStatusById = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      updateFunnelStatusByContactId(id, status),

    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["contacts"] });
      queryClient.invalidateQueries({ queryKey: ["contact", variables.id] });
    },
  });

  return {
    mutationPostContact,
    mutationUpdateContactById,
    mutationUpdateFunnelStatusById,
  };
};
