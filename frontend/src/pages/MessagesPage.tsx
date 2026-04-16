// src/pages/MessagesPage.tsx
import { useState } from "react";
import { ConversationList } from "../components/messages/ConversationList";
import MessagePanel from "../components/messages/MessagePanel";
import { useGetContactsDashboard } from "../services/use_queries/contacts-query";
import type { ContactResType } from "../types/contact.types";
import type { ConversationResType } from "../types/conversation.types";

type ActiveChatType = {
  contact: ContactResType;
  conversations: ConversationResType[];
} | null;

interface MessagesPageProps {
  isAdminView?: boolean;
}

export const MessagesPage = ({ isAdminView = false }: MessagesPageProps) => {

  const [activeChat, setActiveChat] = useState<ActiveChatType>(null);

  const { data: contactsDashboard, isLoading } = useGetContactsDashboard();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-lg font-medium text-primary">Cargando conversaciones...</p>
      </div>
    );
  }

  const contacts = contactsDashboard?.contacts || [];

  return (
    <>
      <div className="grid lg:grid-cols-[30%_1fr] h-[calc(100vh-120px)] gap-4">
        {/* Lista de conversaciones */}
        <div className={`${activeChat ? 'hidden' : 'block'} lg:block`}>
          <ConversationList
            contacts={contacts}
            activeChatId={activeChat?.contact.id ?? null}
            onSelect={(contact, openConversations) =>
              setActiveChat({
                contact,
                conversations: openConversations
              })
            }
            isAdminView={isAdminView}
          />
        </div>

        {/* Panel de mensajes */}
        <div className={`${!activeChat ? 'hidden' : 'block'} lg:block`}>
          <MessagePanel
            contact={activeChat?.contact}
            conversations={activeChat?.conversations || []}
            onBack={() => setActiveChat(null)}
            isAdminView={isAdminView}
          />
        </div>
      </div>
    </>
  );
};