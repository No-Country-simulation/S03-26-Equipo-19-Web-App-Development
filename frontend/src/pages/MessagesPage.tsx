import { useState } from "react";
import { ConversationList } from "../components/messages/ConversationList"
import { useGetConversations } from "../services/use_queries/conversations-query";
import MessagePanel from "../components/messages/MessagePanel";

export const MessagesPage = () => {

  const [activeChatId, setActiveChatId] = useState<number | null>(null);
  const { data: conversationsData = [] } = useGetConversations();

  const activeConversation = conversationsData.find(
    (c) => c.contact.id === activeChatId
  );

  const activeContact = activeConversation?.contact;

  return (
    <div className="grid lg:grid-cols-[30%_1fr] h-screen gap-4">

      <div className={`${activeChatId ? 'hidden' : 'block'} lg:block`}>
        <ConversationList
          conversations={conversationsData}
          activeChatId={activeChatId}
          onSelect={setActiveChatId}
        />
      </div>
      <div className={`${!activeChatId ? 'hidden' : 'block'} lg:block`}>
        <MessagePanel
          conversations={conversationsData}
          contact={activeContact}
          activeChatId={activeChatId}
          onBack={() => setActiveChatId(null)}
        />
      </div>


    </div>
  )
}


