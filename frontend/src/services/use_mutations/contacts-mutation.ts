import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  addTagByContactId,
  postContact,
  removeTagFromContactId,
  updateContactById,
  updateFunnelStatusByContactId,
  assignContactToSalesperson,
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
      queryClient.invalidateQueries({
        queryKey: ["contact", variables.contactId],
      });
    },
  });

  const mutationRemoveTagFromContactId = useMutation({
    mutationFn: ({ contactId, tagId }: { contactId: number; tagId: number }) =>
      removeTagFromContactId(contactId, tagId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["contacts"] });
      queryClient.invalidateQueries({
        queryKey: ["contact", variables.contactId],
      });
    },
  });

  const mutationAssignContact = useMutation({
    mutationFn: ({
      contactId,
      newOwnerId,
    }: {
      contactId: number;
      newOwnerId: number;
    }) => assignContactToSalesperson(contactId, newOwnerId),

    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["contacts"] });
      queryClient.invalidateQueries({
        queryKey: ["contact", variables.contactId],
      });

      // opcional: podés loguear o manejar estado en UI
      console.log("Contacto reasignado correctamente");
    },

    onError: (error: Error) => {
      console.error("Error al reasignar contacto:", error.message);
    },
  });

  return {
    mutationPostContact,
    mutationUpdateContactById,
    mutationUpdateFunnelStatusById,
    mutationAddTagByContactId,
    mutationRemoveTagFromContactId,
    mutationAssignContact,
  };
};