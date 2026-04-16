import { Send, ChevronDown } from "lucide-react"
import { Textarea } from "../ui/textarea"
import { Button } from "../ui/Button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "../ui/dropdown-menu"
import type { Channel, ContactResType } from "../../types/contact.types"
import { useState } from "react"
import { useGetTemplates } from "../../services/use_queries/templates-query"
import { parseTemplate } from "../../utils/parseTemplate"
import { useMessagesMutationsService } from "../../services/use_mutations/messages-mutation";
import type { ConversationResType } from "../../types/conversation.types"



interface Props {
  activeChannel: Channel,
  contact: ContactResType
  activeConversation?: ConversationResType
}

export function MessageInput({ activeChannel, contact, activeConversation }: Props) {

  const { data: templates = [], isLoading } = useGetTemplates()

  const [message, setMessage] = useState("")


  const { mutationPostMessage } = useMessagesMutationsService();

  const filteredTemplates = templates.filter(
    (template) =>
      template.channel.toLowerCase() === activeChannel.toLowerCase()
  )

  const handleSelectTemplate = (templateId: number) => {
    const template = filteredTemplates?.find((template) => template.id === templateId)
    if (template) {
      const parsedMessage = parseTemplate(template.body, {
        name: contact.name
      })
      setMessage(parsedMessage)
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      onSend()
    }
  }

  const onSend = () => {
    if (!message.trim() || mutationPostMessage.isPending) return;

    const conversationId = activeConversation?.id;

    if (!conversationId) return;

    mutationPostMessage.mutate({
      data: {
        contactId: contact.id,
        channel: activeChannel,
        content: { body: message },
      },
      conversationId,
    });
  };


  if (isLoading) return <div className="flex items-center justify-center">
    <p className="text-lg font-medium text-primary">Cargando templates...</p>
  </div>;

  return (
    <div className="flex flex-col md:flex-row gap-2 items-start ">
      <div className="flex justify-between md:flex-col md:items-end">
        <DropdownMenu>
          <DropdownMenuTrigger asChild >
            <Button
              variant="outline"
              size="sm"
              className="flex gap-1.5 h-10 items-center justify-between w-[150px]"
            >
              Plantillas
              <ChevronDown className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="start" className="w-64 bg-white rounded-lg shadow-lg p-2 ring-0">
            <DropdownMenuGroup>
              <DropdownMenuLabel>Plantillas rápidas</DropdownMenuLabel>
              <DropdownMenuSeparator />
              {filteredTemplates?.length === 0 && (
                <div className="p-2 text-sm">
                  No hay plantillas para este canal
                </div>
              )}
              {filteredTemplates?.map((template) => (

                <DropdownMenuItem
                  key={template.id}
                  onClick={() => handleSelectTemplate(template.id)}
                  className="flex flex-col items-start gap-1 py-2"
                >
                  <span className="font-medium">{template.name}</span>
                  <span className="text-xs text-neutro-2/80 line-clamp-1">
                    {template.body.substring(0, 50)}...
                  </span>
                </DropdownMenuItem>

              ))}
            </DropdownMenuGroup>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
      <div className="w-full flex flex-col gap-2 items-end md:flex-row md:items-start">
        <Textarea
          value={message}
          onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setMessage(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={`Escribe un ${activeChannel === "EMAIL" ? "email..." : "mensaje..."}`}
          className="flex-1 min-h-40 max-h-150 resize-none scrollbar-thin scrollbar-thumb-rounded scrollbar-thumb-muted/50"
          rows={1}
        />

        <Button onClick={onSend} disabled={!message.trim() || mutationPostMessage.isPending} variant="secondary"
          className="transition-all flex items-center justify-center mb-15 w-15" >
          <Send size={20} />
        </Button>
      </div>
    </div>
  )
}