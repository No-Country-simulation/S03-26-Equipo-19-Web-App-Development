import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "../../store/useAuthStore";
import type { ConversationInboxItemType, ConversationResType } from "../../types/conversation.types";
import { getConversationById, getConversations, getConversationsByContactId, getConversationsInbox } from "../use_cases/conversation-service";

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


export const useGetConversationsInbox = () => {
  const token = useAuthStore((state) => state.token);
  return useQuery<ConversationInboxItemType[]>({ 
    queryKey: ["conversations-inbox"],
    queryFn: () => getConversationsInbox(token!),
    enabled: !!token,
  });
};    