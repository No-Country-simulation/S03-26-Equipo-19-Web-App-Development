import { ChevronLeft, Mail, MessageCircleMore} from "lucide-react"
import { Tabs, TabsList, TabsTrigger } from "../ui/tabs"

import { Badge } from "../ui/badge"
import type { Channel, ContactResType } from "../../types/contact.types"
import { useCallback} from "react"
import { Avatar } from "../ui/Avatar"

interface Props {
  contact: ContactResType
  channelCounts: Record<Channel, number>
  activeChannel: Channel
  setActiveChannel: (channel: Channel) => void
}

export function ContactHeader({
  contact,
  channelCounts,
  activeChannel = "whatsapp",
  setActiveChannel = () => {},
}: Props) {

 
  const handleBackToList = useCallback(() => {
    window.history.back();
  }, [])


  return (
    <div className="flex items-center gap-4 rounded-t-lg bg-neutro-3 px-6 py-4">
      <button  onClick={handleBackToList}>
        <ChevronLeft className="h-5 w-5 text-primary" />
      </button>

     <Avatar user={{ name: contact.name }} />

      <div className="flex-1">
        <h2 className="font-semibold">{contact.name}</h2>
        <p className="text-sm text-muted-foreground">{contact.email}</p>
      </div>

      <Tabs value={activeChannel} onValueChange={(v) => setActiveChannel(v as Channel)}>
        <TabsList className="bg-neutro-2/50 rounded-2xl">
          <TabsTrigger value="whatsapp">
            <MessageCircleMore className="h-4 w-4 text-success" />
            WhatsApp
            {channelCounts.whatsapp > 0 && (
              <Badge className="border border-primary/30 bg-white text-primary">{channelCounts.whatsapp}</Badge>
            )}
          </TabsTrigger>

          <TabsTrigger value="email">
            <Mail className="h-4 w-4 text-primary" />
            Email
            {channelCounts.email > 0 && (
              <Badge className="border border-primary/30 bg-white text-primary">
                {channelCounts.email}
              </Badge>
            )}
          </TabsTrigger>
        </TabsList>
      </Tabs>
    </div>
  )
}