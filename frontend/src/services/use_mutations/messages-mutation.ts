import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { MessageReqType, MessageResType } from "../../types/message.types";
import { postMessage } from "../use_cases/messages-service";

export const useMessagesMutationsService = () => {
  const queryClient = useQueryClient();

  const mutationPostMessage = useMutation({
    mutationFn: (data: MessageReqType) => postMessage(data),

    onMutate: async (newMessage) => {
      const conversationId = newMessage.contactId; 

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
        conversationId,
        channel: newMessage.channel,
        direction: "OUTBOUND",
        body: newMessage.content.body,
        status: "SENT",
        createdAt: new Date().toISOString(),
        externalId: `temp-${Date.now()}`,
        messageType: "TEXT",
      };

      queryClient.setQueryData<MessageResType[]>(
        ["messages", conversationId],
        (old = []) => [...old, optimisticMessage],
      );

      return { previous, conversationId };
    },

    onError: (_err, _vars, context) => {
      if (context?.previous) {
        queryClient.setQueryData(
          ["messages", context.conversationId],
          context.previous,
        );
      }
    },

    onSettled: (_data, _err, _vars, context) => {
      queryClient.invalidateQueries({
        queryKey: ["messages", context?.conversationId],
      });

      queryClient.invalidateQueries({
        queryKey: ["conversations"],
      });
    },
  });

  return {
    mutationPostMessage,
  };
};
