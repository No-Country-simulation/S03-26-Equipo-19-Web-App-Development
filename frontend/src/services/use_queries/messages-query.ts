import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "../../store/useAuthStore";
import { getMessages, getMessagesByConversationId } from "../use_cases/messages-service";
import type { MessageResType } from "../../types/message.types";

export const useGetMessages = () => {
  const token = useAuthStore((state) => state.token);
  return useQuery<MessageResType[]>({
    queryKey: ["messages"],
    queryFn: () => getMessages(token!),
    enabled: !!token,
  });
};

export const useGetMessagesByConversationId = (conversationId?: number) => {
  const token = useAuthStore((state) => state.token);
  console.log("messages query conversationId:", conversationId);
  return useQuery<MessageResType[]>({
    queryKey: ["messages", conversationId],
    queryFn: () => getMessagesByConversationId(token!, conversationId!),
    enabled: !!token && conversationId !== undefined,
  });
};  