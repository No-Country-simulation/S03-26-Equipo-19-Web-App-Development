import { Building2, Mail, Smartphone, UserRoundPen } from "lucide-react"

import type { ContactResType } from "../../types/contact.types"
import AvatarContact from "../ui/AvatarContact"


interface Props {
  contact: ContactResType
openModal: () => void
}

export function ContactCard({ contact, openModal }: Props) {


  return (
    <div className="bg-white border-border shadow-md rounded-lg flex justify-between items-start p-4">
      <div className="flex flex-col lg:flex-row items-start gap-5 px-3 lg:px-6 py-4">
        <AvatarContact name={contact.name} lastName={contact.lastName} size="lg" />
        <div className="mt-3">
          <h3 className="font-semibold text-lg text-foreground">
            {contact.name} {contact.lastName}
          </h3>
          <div className="mt-6 flex flex-col gap-0.5 w-full text-sm">
            <div className="flex items-center gap-3 text-muted-foreground hover:text-foreground transition-colors rounded-lg p-1 hover:bg-muted/50">
              <Smartphone className="h-4 w-4 shrink-0 text-primary" />
              <span>{contact.phone ?? "No hay dato"}</span>
            </div>
            <div className="flex items-center gap-3 text-muted-foreground hover:text-foreground transition-colors rounded-lg p-1 hover:bg-muted/50">
              <Mail className="h-4 w-4 shrink-0 text-primary" />
              <span className="truncate">{contact.email ?? "No hay dato"}</span>
            </div>
            <div className="flex items-center gap-3 text-muted-foreground hover:text-foreground transition-colors rounded-lg p-1 hover:bg-muted/50">
              <Building2 className="h-4 w-4 shrink-0 text-primary" />
              <span>{contact.company ?? "No hay dato"}</span>
            </div>
          </div>
        </div>
      </div>
        <UserRoundPen className="w-6 h-6 text-primary lg:mt-4 lg:mr-4" onClick={openModal} />
    </div>
  )
}