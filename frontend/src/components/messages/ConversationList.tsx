import type { Conversation } from '../../types/message.types';

interface ConversationListProps {
  conversations: Conversation[];
  activeId: string | null;
  onSelect: (contactId: string) => void;
}

const formatTime = (iso: string) => {
  const date = new Date(iso);
  return date.toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' });
};

export const ConversationList = ({ conversations, activeId, onSelect }: ConversationListProps) => {
  return (
    <div className="flex flex-col h-full overflow-y-auto">
      {conversations.map(conv => (
        <button
          key={conv.contactId}
          onClick={() => onSelect(conv.contactId)}
          className={`
            flex items-start gap-3 px-4 py-4 text-left border-b border-gray-800
            hover:bg-gray-800/60 transition-colors
            ${activeId === conv.contactId ? 'bg-gray-800' : ''}
          `}
        >
          {/* Avatar */}
          <div className="w-10 h-10 rounded-full bg-sky-500/20 text-sky-400 flex items-center justify-center font-semibold text-sm shrink-0">
            {conv.contactName.charAt(0)}
          </div>

          <div className="flex-1 min-w-0">
            <div className="flex justify-between items-center mb-0.5">
              <span className="text-white text-sm font-medium truncate">{conv.contactName}</span>
              <span className="text-gray-500 text-xs shrink-0 ml-2">{formatTime(conv.lastMessageAt)}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-400 text-xs truncate">{conv.lastMessage}</span>
              {conv.unreadCount > 0 && (
                <span className="bg-sky-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center shrink-0 ml-2">
                  {conv.unreadCount}
                </span>
              )}
            </div>
            <span className="text-xs text-gray-600 capitalize mt-0.5 block">{conv.channel}</span>
          </div>
        </button>
      ))}
    </div>
  );
};