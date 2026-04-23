import { useQuery, useQueryClient } from "@tanstack/react-query";
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

import { useEffect, useRef } from "react";

export const useGetMessagesByConversationId = (conversationId?: number) => {
  const token = useAuthStore((state) => state.token);
  const queryClient = useQueryClient();

  // 👉 para evitar duplicaciones por polling
  const lastMessageIdRef = useRef<number | null>(null);

  const query = useQuery<MessageResType[]>({
    queryKey: ["messages", conversationId],
    queryFn: () => getMessagesByConversationId(token!, conversationId!),
    enabled: !!token && conversationId !== undefined,
  });

  useEffect(() => {
    const messages = query.data;
    if (!messages?.length) return;

    const last = messages[messages.length - 1];

    // 🚨 evitar procesar el mismo mensaje múltiples veces
    if (lastMessageIdRef.current === last.id) return;
    lastMessageIdRef.current = last.id;

    const convId = last.conversation.id;

    // =========================
    // 🔥 UPDATE INBOX
    // =========================
    queryClient.setQueryData(["conversations-inbox"], (old: any[] = []) => {
      const exists = old.find(c => c.conversationId === convId);
      if (!exists) return old;

      const updated = {
        ...exists,
        lastMessagePreview: last.body,
        lastMessageAt: last.sentAt,
        unreadCount:
          last.direction === "INBOUND"
            ? (exists.unreadCount || 0) + 1
            : exists.unreadCount,
      };

      // 🔥 reorder tipo WhatsApp
      const filtered = old.filter(c => c.conversationId !== convId);
      return [updated, ...filtered];
    });

    // =========================
    // 🔥 UPDATE DASHBOARD
    // =========================
    queryClient.setQueryData(["contacts-dashboard"], (old: any) => {
      if (!old) return old;

      return {
        ...old,
        contacts: old.contacts.map((contact: any) => ({
          ...contact,
          conversations: contact.conversations.map((conv: any) =>
            conv.id === convId
              ? {
                  ...conv,
                  unreadCount:
                    last.direction === "INBOUND"
                      ? (conv.unreadCount || 0) + 1
                      : conv.unreadCount,
                }
              : conv
          ),
        })),
      };
    });

  }, [query.data, queryClient]);

  return query;
};