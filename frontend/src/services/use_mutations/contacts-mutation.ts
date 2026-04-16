import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  addTagByContactId,
  postContact,
  removeTagFromContactId,
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
      queryClient.invalidateQueries({ queryKey: ["metrics-contacts"] });
    },
  });

  const mutationUpdateContactById = useMutation({
    mutationFn: ({ id, data }: { id: number; data: ContactReqType }) =>
      updateContactById(id, data),

    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["contacts"] });
      queryClient.invalidateQueries({ queryKey: ["contact", variables.id] });
      queryClient.invalidateQueries({ queryKey: ["metrics-contacts"] });
    },
  });

  const mutationUpdateFunnelStatusById = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      updateFunnelStatusByContactId(id, status),

    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["contacts"] });
      queryClient.invalidateQueries({ queryKey: ["contact", variables.id] });
      queryClient.invalidateQueries({ queryKey: ["metrics-contacts"] });
    },
  });

  const mutationAddTagByContactId = useMutation({
    mutationFn: ({ contactId, tagId }: { contactId: number; tagId: number }) =>
      addTagByContactId(contactId, tagId),

    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["contacts"] });
      queryClient.invalidateQueries({ queryKey: ["contact", variables.contactId] });
      queryClient.invalidateQueries({ queryKey: ["metrics-contacts"] });
      },
  });

  const mutationRemoveTagFromContactId = useMutation({
    mutationFn: ({ contactId, tagId }: { contactId: number; tagId: number }) =>
      removeTagFromContactId(contactId, tagId),

    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["contacts"] });
      queryClient.invalidateQueries({ queryKey: ["contact", variables.contactId] });
      queryClient.invalidateQueries({ queryKey: ["metrics-contacts"] });

    },
  });


  return {
    mutationPostContact,
    mutationUpdateContactById,
    mutationUpdateFunnelStatusById,
    mutationAddTagByContactId,
    mutationRemoveTagFromContactId,
  };
};

