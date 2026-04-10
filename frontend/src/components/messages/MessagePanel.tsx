import React, { useMemo, useState } from 'react'
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
}


const MessagePanel = ({ conversations, contact, activeChatId, onBack}: MessagePanelProps) => {

    const [activeChannel, setActiveChannel] = useState<Channel>("WHATSAPP")

    const resolvedContact = contact ?? conversations.find(
        c => c.contact.id === activeChatId
    )?.contact;

    const activeConversation = conversations.find(
        (c) =>
            c.contact.id === resolvedContact?.id &&
            c.channel === activeChannel &&
            c.status === "OPEN"
    );
    const conversationId = activeConversation?.id;

    const { data: messagesData = [], isLoading } = useGetMessagesByConversationId(conversationId);

    const channelCounts = useMemo(() => {
        return {
            WHATSAPP: messagesData.filter((m) => m.channel === "WHATSAPP").length,
            EMAIL: messagesData.filter((m) => m.channel === "EMAIL").length,
        }
    }, [messagesData, activeChannel])

    const hasConversation = Boolean(activeConversation);

    if (!activeChatId && !contact) {
        return <div className="flex items-center justify-center"> <p className="text-lg font-medium text-primary">Seleccioná una conversación </p></div>;
    }

    return (
        <div className="flex flex-col gap-4">
            <div className=" bg-white rounded-lg shadow">
                <ContactHeader contact={contact!} channelCounts={channelCounts} activeChannel={activeChannel} setActiveChannel={setActiveChannel} onBack={onBack}/>
                <ConversationPanel messages={messagesData} activeConversation={hasConversation} isLoading={isLoading} />
            </div>
            <MessageInput contact={contact!} activeChannel={activeChannel} />
        </div>
    )
}

export default MessagePanel
