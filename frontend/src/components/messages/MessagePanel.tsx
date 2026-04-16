// src/components/messages/MessagePanel.tsx
import { useMemo, useState } from 'react'
import { ContactHeader } from '../contacts/ContactHeader'
import type { ConversationResType } from '../../types/conversation.types'
import { ConversationPanel } from '../contacts/ConversationPanel'
import type { Channel, ContactResType } from '../../types/contact.types'
import { MessageInput } from '../contacts/MessageInput'
import { useGetMessagesByConversationId } from '../../services/use_queries/messages-query'

interface MessagePanelProps {
    conversations: ConversationResType[]
    contact?: ContactResType
    activeChatId?: number | null
    onBack?: () => void
    isAdminView?: boolean
}

const MessagePanel = ({ conversations, contact, activeChatId, onBack, isAdminView = false }: MessagePanelProps) => {

    const [activeChannel, setActiveChannel] = useState<Channel>("WHATSAPP")

    const activeConversation = conversations.find(
        (c) => c.channel === activeChannel && c.status === "OPEN"
    );
    const conversationId = activeConversation?.id;

    const { data: messagesData = [], isLoading } = useGetMessagesByConversationId(conversationId);

    console.log({messagesData});
    

    const channelCounts = useMemo(() => {
        return {
            WHATSAPP: conversations
                .filter(c => c.channel === "WHATSAPP")
                .reduce((acc, c) => acc + (c.unreadCount || 0), 0),
            EMAIL: conversations
                .filter(c => c.channel === "EMAIL")
                .reduce((acc, c) => acc + (c.unreadCount || 0), 0),
        };
    }, [conversations]);

    const hasConversation = !!activeConversation;

    if (!activeChatId && !contact) {
        return <div className="flex items-center justify-center h-150 bg-white/35 shadow-lg"> 
            <p className="text-lg font-medium text-primary">Seleccioná un contacto</p>
        </div>;
    }

    return (
        <div className="flex flex-col gap-4">
            <div className="bg-white rounded-lg shadow">
                <ContactHeader 
                    contact={contact!} 
                    channelCounts={channelCounts} 
                    activeChannel={activeChannel} 
                    setActiveChannel={setActiveChannel} 
                    onBack={onBack} 
                />
                <ConversationPanel 
                    messages={messagesData} 
                    activeConversation={hasConversation} 
                    isLoading={isLoading} 
                />
            </div>
            <MessageInput contact={contact!} activeChannel={activeChannel} />
        </div>
    )
}

export default MessagePanel