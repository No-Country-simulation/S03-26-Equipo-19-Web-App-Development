import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { MessageReqType, MessageResType } from "../../types/message.types";
import {
  postMessage,
  readAllMessagesInConversationById,
} from "../use_cases/messages-service";


export const useMessagesMutationsService = () => {
  const queryClient = useQueryClient();

 const mutationPostMessage = useMutation({
  mutationFn: ({
    data,
  }: {
    data: MessageReqType;
    conversationId: number;
  }) => postMessage(data),

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
      (old = []) => [...old, optimisticMessage]
    );

    return { previous, conversationId };
  },

  onError: (_err, _vars, context) => {
    if (!context) return;

    queryClient.setQueryData(
      ["messages", context.conversationId],
      context.previous
    );
  },

  onSuccess: (_data, _vars, context) => {
    if (!context) return;

    queryClient.invalidateQueries({
      queryKey: ["messages", context.conversationId],
    });

    queryClient.invalidateQueries({
      queryKey: ["conversations"],
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

      queryClient.invalidateQueries({
        queryKey: ["messages", conversationId],
      });

      queryClient.invalidateQueries({
        queryKey: ["conversations"],
      });
      console.log("invalidate conversationId:", conversationId);
    },
    
  });

  return {
    mutationPostMessage,
    mutationReadAllMessages,
  };
};
