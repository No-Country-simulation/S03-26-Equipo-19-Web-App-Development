import { DashboardLayout } from '../components/layout/DashboardLayout';
import { ConversationList } from '../components/messages/ConversationList';
import { ChatWindow } from '../components/messages/ChatWindow';
import { useMessages } from '../hooks/useMessages';

export const MessagesPage = () => {
  const { conversations, activeConversation, loading, sendMessage, selectConversation } = useMessages();

  return (
    <DashboardLayout>
      <h1 className="text-white text-2xl font-bold mb-6">Mensajes</h1>

      {loading && <p className="text-gray-400 text-sm">Cargando conversaciones...</p>}

      {!loading && (
        <div className="bg-gray-900 border border-gray-700 rounded-2xl overflow-hidden flex h-[calc(100vh-220px)]">
          {/* Lista de conversaciones */}
          <div className="w-80 border-r border-gray-700 shrink-0">
            <ConversationList
              conversations={conversations}
              activeId={activeConversation?.contactId ?? null}
              onSelect={selectConversation}
            />
          </div>

          {/* Panel de chat */}
          <div className="flex-1">
            {activeConversation ? (
              <ChatWindow
                conversation={activeConversation}
                onSend={sendMessage}
              />
            ) : (
              <div className="flex items-center justify-center h-full text-gray-500">
                Seleccioná una conversación
              </div>
            )}
          </div>
        </div>
      )}
    </DashboardLayout>
  );
};