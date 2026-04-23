import { ScrollArea, ScrollBar } from "../ui/scroll-area"
import { MessageSquare } from "lucide-react"
import { ChatMessage } from "../contacts/ChatMessage"
import { useEffect, useRef } from "react"
import type { MessageResType } from "../../types/message.types"

interface Props {
  messages: MessageResType[]
  activeConversation: boolean
  isLoading: boolean  
}

export function ConversationPanel({messages, activeConversation, isLoading}: Props) {


  const bottomRef = useRef<HTMLDivElement>(null);


  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);


  return (
  <ScrollArea className="h-[calc(100vh-400px)] p-6">
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
      <ScrollBar orientation="vertical" />
    </ScrollArea>
  )
}