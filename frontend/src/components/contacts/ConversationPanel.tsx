import { ScrollArea } from "../ui/scroll-area"
import { MessageSquare } from "lucide-react"
import { ChatMessage } from "../contacts/ChatMessage"
import type { MessageResType } from "../../types/message.types"
import type { Channel } from "../../types/contact.types"

interface Props {
  messages: MessageResType[]
  activeChannel: Channel
}

export function ConversationPanel({ messages, activeChannel }: Props) {
  const filtered = messages.filter((m) => m.channel === activeChannel)

  return (
    <ScrollArea className="flex-1 p-6">
      {filtered.length === 0 ? (
        <div className="text-center py-20">
          <MessageSquare className="mx-auto mb-4" />
          <p>No messages yet</p>
        </div>
      ) : (
        filtered.map((m) => <ChatMessage key={m.id} message={m} />)
      )}
    </ScrollArea>
  )
}