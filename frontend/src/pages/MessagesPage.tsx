import { useState } from "react";
import { ConversationList } from "../components/messages/ConversationList";
import MessagePanel from "../components/messages/MessagePanel";
import { useGetContactsDashboard } from "../services/use_queries/contacts-query";

export const MessagesPage = () => {
  const [activeContactId, setActiveContactId] = useState<number | null>(null);

  const { data: contactsDashboard, isLoading } = useGetContactsDashboard();

  const contacts = contactsDashboard?.contacts || [];

  const activeContact = contacts.find(
    (c) => c.id === activeContactId
  );

  const activeConversations = activeContact?.conversations ?? [];

  if (isLoading) {
    return <p>Cargando conversaciones...</p>;
  }

  return (
    <div className="grid lg:grid-cols-[30%_1fr] h-[calc(100vh-120px)] gap-4">

      <div className={`${activeContact ? 'hidden' : 'block'} lg:block`}>
        <ConversationList
          contacts={contacts}
          activeChatId={activeContactId}
          onSelect={(contact) =>
            setActiveContactId(contact.id)
          }
        />
      </div>

      <div className={`${!activeContact ? 'hidden' : 'block'} lg:block`}>
        <MessagePanel
          contact={activeContact}
          conversations={activeConversations}
          onBack={() => setActiveContactId(null)}
        />
      </div>

    </div>
  );
};