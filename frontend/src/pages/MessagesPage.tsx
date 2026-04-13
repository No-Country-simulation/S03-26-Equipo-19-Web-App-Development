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

export const MessagesPage = () => {

  const [activeChat, setActiveChat] = useState<ActiveChatType>(null);

  const { data: contactsDashboard, isLoading } = useGetContactsDashboard();


if (isLoading) {
  return <div>Cargando...</div>;
}


  return (
    <div className="grid lg:grid-cols-[30%_1fr] h-screen gap-4">

      <div className={`${activeChat ? 'hidden' : 'block'} lg:block`}>
        <ConversationList
          contacts={contactsDashboard?.contacts!}
          activeChatId={activeChat?.contact.id ?? null}
          onSelect={(contact, openConversations) =>
            setActiveChat({
              contact,
              conversations: openConversations
            })
          }
        />
      </div>

      <div className={`${!activeChat ? 'hidden' : 'block'} lg:block`}>
        <MessagePanel
          contact={activeChat?.contact}
          conversations={activeChat?.conversations || []}
          onBack={() => setActiveChat(null)}
        />
      </div>

    </div>
  );
};