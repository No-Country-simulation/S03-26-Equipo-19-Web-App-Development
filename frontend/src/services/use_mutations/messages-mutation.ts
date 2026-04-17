import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { MessageReqType, MessageResType } from "../../types/message.types";
import {
  postMessage,
  readAllMessagesInConversationById,
} from "../use_cases/messages-service";

export const useMessagesMutationsService = () => {
  const queryClient = useQueryClient();

  const mutationPostMessage = useMutation({
    mutationFn: async ({
      data,
    }: {
      data: MessageReqType;
      conversationId?: number;
    }) => {
      return postMessage(data);
    },

    onMutate: async ({ data, conversationId }) => {
      await queryClient.cancelQueries({
        queryKey: ["messages", conversationId],
      });

      const previous = queryClient.getQueryData<MessageResType[]>([
        "messages",
        conversationId,
      ]);

      const optimisticMessage: MessageResType = {
        id: Date.now(),
        userId: 0,
        conversation: {
          id: conversationId,
          channel: data.channel,
        } as MessageResType["conversation"],
        direction: "OUTBOUND",
        body: data.content.body,
        deliveryStatus: "SENT",
        sentAt: new Date().toISOString(),
        providerId: "",
        sender: null,
        template: null,
      };

      queryClient.setQueryData<MessageResType[]>(
        ["messages", conversationId],
        (old = []) => [...old, optimisticMessage],
      );

      if (conversationId) {
        queryClient.setQueryData<any[]>(["conversations-inbox"], (old = []) => {
          const exists = old.some((c) => c.conversationId === conversationId);

          if (exists) {
            return old.map((c) =>
              c.conversationId === conversationId
                ? {
                    ...c,
                    lastMessagePreview: data.content.body,
                    lastMessageAt: new Date().toISOString(),
                  }
                : c,
            );
          }

          return [
            {
              conversationId,
              channel: data.channel,
              lastMessagePreview: data.content.body,
              lastMessageAt: new Date().toISOString(),
              contactId: data.contactId,
              contactName: "",
              contactIdentifier: "",
            },
            ...old,
          ];
        });
      }

      return { previous, conversationId };
    },

    onError: (_err, _vars, context) => {
      if (!context) return;

      queryClient.setQueryData(
        ["messages", context.conversationId],
        context.previous,
      );
    },

    onSuccess: (_data, _vars, context) => {
      if (!context) return;

      queryClient.invalidateQueries({
        queryKey: ["conversations"],
      });

      queryClient.invalidateQueries({
        queryKey: ["contacts"],
      });

      queryClient.invalidateQueries({
        queryKey: ["contacts-dashboard"],
      });
    },
  });

  const mutationReadAllMessages = useMutation({
    mutationFn: (conversationId: number) =>
      readAllMessagesInConversationById(conversationId),

    onSuccess: (_, conversationId) => {
      queryClient.setQueryData(["conversations"], (old: any[]) =>
        old.map((c) =>
          c.id === conversationId ? { ...c, unreadCount: 0 } : c,
        ),
      );

      queryClient.invalidateQueries({ queryKey: ["messages", conversationId] });
      queryClient.invalidateQueries({ queryKey: ["conversations"] });
      queryClient.invalidateQueries({ queryKey: ["contacts-dashboard"] });
      queryClient.invalidateQueries({ queryKey: ["conversations-inbox"] });
    },
  });

  return {
    mutationPostMessage,
    mutationReadAllMessages,
  };
};
