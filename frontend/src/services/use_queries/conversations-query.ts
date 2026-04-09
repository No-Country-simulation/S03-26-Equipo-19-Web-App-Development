import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "../../store/useAuthStore";
import type { ConversationResType } from "../../types/conversation.types";
import { getConversationById, getConversations, getConversationsByContactId } from "../use_cases/conversation-service";

export const useGetConversations = () => {
  const token = useAuthStore((state) => state.token);
  return useQuery<ConversationResType[]>({
    queryKey: ["conversations"],
    queryFn: () => getConversations(token!),
    enabled: !!token,
  });
};

export const useGetConversationById = (contactId: number) => {
  const token = useAuthStore((state) => state.token);
  return useQuery<ConversationResType>({
    queryKey: ["conversation", contactId],
    queryFn: () => getConversationById(token!, contactId),
    enabled: !!token && !!contactId,
  });
};  

export const useGetConversationsByContactId = (contactId: number) => {
  const token = useAuthStore((state) => state.token);
  return useQuery<ConversationResType[]>({
    queryKey: ["conversation", contactId],
    queryFn: () => getConversationsByContactId(token!, contactId),
    enabled: !!token && contactId !== undefined,
  });
};  