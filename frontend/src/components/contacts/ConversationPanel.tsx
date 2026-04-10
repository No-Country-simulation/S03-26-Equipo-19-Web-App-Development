import { ScrollArea } from "../ui/scroll-area"
import { MessageSquare } from "lucide-react"
import { ChatMessage } from "../contacts/ChatMessage"

import type { Channel } from "../../types/contact.types"
import type { ConversationResType } from "../../types/conversation.types"
import { useGetMessagesByConversationId } from "../../services/use_queries/messages-query"
import { useEffect, useRef } from "react"

interface Props {
  conversations: ConversationResType[]
  activeChannel: Channel
}

export function ConversationPanel({ conversations, activeChannel }: Props) {

  const openConversations = conversations.filter((c) => c.status === "OPEN")

  const activeConversation = openConversations.find(
    (c) => c.channel === activeChannel
  );

  const conversationId = activeConversation?.id;

  const { data: messages = [], isLoading } = useGetMessagesByConversationId(conversationId);

  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  return (
    <ScrollArea className="h-[calc(100vh-200px)] p-6">
      {!activeConversation ? (
        <div className="text-center py-20">
          <MessageSquare className="mx-auto mb-4" />
          <p>No hay conversación abierta en este canal</p>
        </div>
      ) : isLoading ? (
        <p>Cargando mensajes...</p>
      ) : !messages || messages.length === 0 ? (
        <p>No hay mensajes</p>
      ) : (
        <>
          {messages.map((m) => <ChatMessage key={m.id} message={m} />)}
          <div ref={bottomRef} />
        </>
      )}
    </ScrollArea>
  )
}