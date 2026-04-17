// src/pages/MessagesPage.tsx
import { useState } from "react";
import { ConversationList } from "../components/messages/ConversationList";
import MessagePanel from "../components/messages/MessagePanel";
import { useGetContactsDashboard } from "../services/use_queries/contacts-query";
import TitleSection from "../components/ui/TitleSection";

interface MessagesPageProps {
  isAdminView?: boolean;
}

export const MessagesPage = ({ isAdminView = false }: MessagesPageProps) => {
  const [activeContactId, setActiveContactId] = useState<number | null>(null);

  const { data: contactsDashboard, isLoading } = useGetContactsDashboard();

  const contacts = contactsDashboard?.contacts || [];

  const activeContact = contacts.find(
    (c) => c.id === activeContactId
  );

  const activeConversations = activeContact?.conversations ?? [];

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-lg font-medium text-primary">Cargando conversaciones...</p>
      </div>
    );
  }

  return (
    <>
      {/* Título dinámico según el rol */}
      {isAdminView && (
        <div className="mb-6">
          <TitleSection text="Todas las conversaciones" className="hidden md:flex" />
        </div>
      )}

      <div className="grid lg:grid-cols-[30%_1fr] h-[calc(100vh-120px)] gap-4">
        <div className={`${activeContact ? 'hidden' : 'block'} lg:block`}>
          <ConversationList
            contacts={contacts}
            activeChatId={activeContactId}
            onSelect={(contact) => setActiveContactId(contact.id)}
            isAdminView={isAdminView}
          />
        </div>

        <div className={`${!activeContact ? 'hidden' : 'block'} lg:block`}>
          <MessagePanel
            contact={activeContact}
            conversations={activeConversations}
            onBack={() => setActiveContactId(null)}
            isAdminView={isAdminView}
          />
        </div>
      </div>
    </>
  );
};