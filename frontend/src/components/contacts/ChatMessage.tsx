
import { Check, CheckCheck, Mail, MessageCircleMore } from "lucide-react"

import { cn } from "../../lib/utils"
import type { MessageResType } from "../../types/message.types"


interface ChatMessageProps {
  message: MessageResType
  showChannelIndicator?: boolean
}

export function ChatMessage({ message, showChannelIndicator = false }: ChatMessageProps) {
  const StatusIcon = message.status === "READ" ? CheckCheck : Check
  const isEmail = message.channel === "EMAIL"

  return (
    <div
      className={cn(
        "flex w-full",
        message.direction === "OUTBOUND" ? "justify-end" : "justify-start"
      )}
    >
      <div
        className={cn(
          "max-w-[75%] rounded-2xl px-4 py-2.5 shadow-sm mb-2",
          message.direction === "OUTBOUND"
            ? "bg-secondary/15 text-primary-foreground rounded-br-md"
            : "bg-neutro-2/25 text-foreground rounded-bl-md "
        )}
      >
        {showChannelIndicator && (
          <div
            className={cn(
              "mb-1.5 flex items-center gap-1.5 text-xs",
              message.direction === "OUTBOUND"
                ? "text-primary/10"
                : "text-muted-foreground"
            )}
          >
            {isEmail ? (
              <>
                <Mail className="h-3 w-3 text-primary" />
                <span>Email</span>
              </>
            ) : (
              <>
                <MessageCircleMore className="h-3 w-3 text-success" />
                <span>WhatsApp</span>
              </>
            )}
          </div>
        )}
        <p className={cn(
          "text-sm leading-relaxed",
          isEmail && "whitespace-pre-line"
        )}>
          {message.body}
        </p>
        <div
          className={cn(
            "mt-1.5 flex items-center justify-end gap-1 text-xs",
            message.direction === "OUTBOUND"
              ? "text-primary-foreground/70" 
              : "text-muted-foreground"
          )}
        >
          <span>{message.createdAt}</span>
          {message.direction === "OUTBOUND" && (
            <StatusIcon
              className={cn(
                "h-3.5 w-3.5",
                message.status === "READ" && message.direction === "OUTBOUND" && "text-success"
              )}
            />
          )}
        </div>
      </div>
    </div>
  )
}
