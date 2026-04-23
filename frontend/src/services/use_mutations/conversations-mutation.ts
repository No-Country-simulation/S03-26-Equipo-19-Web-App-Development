import { useMutation, useQueryClient } from "@tanstack/react-query";
import { closeConversationById, reOpenConversationById } from "../use_cases/conversation-service";
import type { ConversationResType } from "../../types/conversation.types";


export const useConversationsMutationsService = () => {
  const queryClient = useQueryClient();


  const updateConversationStatusInCache = (
  old: ConversationResType[] | undefined,
  id: number,
  status: "OPEN" | "CLOSED"
) => {
  return old?.map((c) =>
    c.id === id ? { ...c, status } : c
  );
};
 
const mutationCloseConversationById = useMutation({
  mutationFn: ({ id }: { id: number }) => closeConversationById(id),

  onMutate: async ({ id }) => {
    await queryClient.cancelQueries();

    const previousConversations = queryClient.getQueriesData<ConversationResType[]>({
      queryKey: ["conversations"],
    });

   /*  const previousByContact = queryClient.getQueriesData<ConversationResType[]>({
      queryKey: ["conversation"],
    }); */

    queryClient.setQueriesData(
      { queryKey: ["conversations"] },
      (old: ConversationResType[] | undefined) => updateConversationStatusInCache(old, id, "CLOSED")
    );

  /*   queryClient.setQueriesData(
      { queryKey: ["conversation"] },
      (old: ConversationResType[] | undefined) => updateConversationStatusInCache(old, id, "CLOSED")
    ); */

    return { previousConversations/* , previousByContact  */};
  },

  onError: (_err, _vars, context) => {
    context?.previousConversations?.forEach(([key, data]) => {
      queryClient.setQueryData(key, data);
    });

    /* context?.previousByContact?.forEach(([key, data]) => {
      queryClient.setQueryData(key, data);
    }); */
  },

  onSettled: () => {
    queryClient.invalidateQueries({ queryKey: ["conversations"] });
    queryClient.invalidateQueries({ queryKey: ["conversation"] });
     queryClient.invalidateQueries({queryKey: ["contacts-dashboard"] });
    queryClient.invalidateQueries({ queryKey: ["conversations-inbox"] });
    queryClient.invalidateQueries({ queryKey: ["metrics-conversations"] });
  },
});

const mutationReopenConversationById = useMutation({
  mutationFn: ({ id }: { id: number }) => reOpenConversationById(id),

  onMutate: async ({ id }) => {
    await queryClient.cancelQueries();

    const previousConversations = queryClient.getQueriesData<ConversationResType[]>({
      queryKey: ["conversations"],
    });

    const previousByContact = queryClient.getQueriesData<ConversationResType[]>({
      queryKey: ["conversation"],
    });

    queryClient.setQueriesData(
      { queryKey: ["conversations"] },
      (old: ConversationResType[] | undefined) => updateConversationStatusInCache(old, id, "OPEN")
    );

    queryClient.setQueriesData(
      { queryKey: ["conversation"] },
      (old: ConversationResType[] | undefined) => updateConversationStatusInCache(old, id, "OPEN")
    );

    return { previousConversations, previousByContact };
  },

  onError: (_err, _vars, context) => {
    context?.previousConversations?.forEach(([key, data]) => {
      queryClient.setQueryData(key, data);
    });

    context?.previousByContact?.forEach(([key, data]) => {
      queryClient.setQueryData(key, data);
    });
  },

  onSettled: () => {
    queryClient.invalidateQueries({ queryKey: ["conversations"] });
    queryClient.invalidateQueries({ queryKey: ["conversation"] });
      queryClient.invalidateQueries({queryKey: ["contacts-dashboard"] });
    queryClient.invalidateQueries({ queryKey: ["conversations-inbox"] });
    queryClient.invalidateQueries({ queryKey: ["metrics-conversations"] });
  },
});

  return {
    mutationCloseConversationById,
    mutationReopenConversationById
  };
};
