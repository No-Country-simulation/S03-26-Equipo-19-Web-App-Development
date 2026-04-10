import { ChevronLeft, Mail, MessageCircleMore } from "lucide-react"
import { Tabs, TabsList, TabsTrigger } from "../ui/tabs"


import type { Channel, ContactResType } from "../../types/contact.types"
import { useCallback } from "react"
import { Badge } from "../ui/Badge"
import AvatarContact from "../ui/AvatarContact"


interface Props {
  contact: ContactResType
  channelCounts: Record<Channel, number>
  activeChannel: Channel
  setActiveChannel: (channel: Channel) => void
}

export function ContactHeader({
  contact,
  channelCounts,
  activeChannel = "WHATSAPP",
  setActiveChannel = () => { },
}: Props) {


  const handleBackToList = useCallback(() => {
    window.history.back();
  }, [])


  return (
    <div className="flex flex-col md:flex-row md:justify-between items-center gap-4 rounded-t-lg bg-neutro-3 py-4">
      <div className="flex items-center gap-4 rounded-t-lg bg-neutro-3 lg:px-6 py-4">
        <button onClick={handleBackToList}>
          <ChevronLeft className="h-5 w-5 text-primary" />
        </button>

        <AvatarContact name={contact.name} lastName={contact.lastName} />

        <div className="flex-1">
          <h2 className="font-semibold">{contact.name} {contact.lastName}</h2>
          <p className="text-sm text-muted-foreground">{contact.email}</p>
        </div>
      </div>

      <Tabs value={activeChannel} onValueChange={(v) => setActiveChannel(v as Channel)} className={"md:pr-6"}>
        <TabsList className="bg-neutro-2/50 rounded-2xl">
          <TabsTrigger value="WHATSAPP">
            <MessageCircleMore className="h-4 w-4 text-success" />
            WhatsApp
            {channelCounts.WHATSAPP > 0 && (
              <Badge className="border border-primary/30 bg-white text-primary">{channelCounts.WHATSAPP}</Badge>
            )}
          </TabsTrigger>

          <TabsTrigger value="EMAIL">
            <Mail className="h-4 w-4 text-primary" />
            Email
            {channelCounts.EMAIL > 0 && (
              <Badge className="border border-primary/30 bg-white text-primary">
                {channelCounts.EMAIL}
              </Badge>
            )}
          </TabsTrigger>
        </TabsList>
      </Tabs>
    </div>
  )
}