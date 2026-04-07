import { Building2, Mail, Phone } from "lucide-react"

import type { ContactResType } from "../../types/contact.types"
import {Avatar} from "../ui/Avatar"


interface Props {
  contact: ContactResType

}

export function ContactCard({ contact }: Props) {


  return (
    <div className="bg-white border-border shadow-md rounded-lg">
      <div className="p-5">
        <div className="flex flex-col items-center text-center">
          <Avatar user={{ name: contact.name }} />
          <h3 className="font-semibold text-lg text-foreground">
            {contact.name}
          </h3>
          <div className="mt-4 flex flex-col gap-2.5 w-full text-sm">
            <div className="flex items-center gap-3 text-muted-foreground hover:text-foreground transition-colors rounded-lg p-2 hover:bg-muted/50">
              <Mail className="h-4 w-4 shrink-0 text-primary" />
              <span className="truncate">{contact.email}</span>
            </div>
            <div className="flex items-center gap-3 text-muted-foreground hover:text-foreground transition-colors rounded-lg p-2 hover:bg-muted/50">
              <Phone className="h-4 w-4 shrink-0 text-primary" />
              <span>{contact.phone}</span>
            </div>
            <div className="flex items-center gap-3 text-muted-foreground hover:text-foreground transition-colors rounded-lg p-2 hover:bg-muted/50">
              <Building2 className="h-4 w-4 shrink-0 text-primary" />
              <span>Acme Corporation</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}