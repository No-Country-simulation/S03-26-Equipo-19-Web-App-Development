import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select'
import type { ContactResType, FunnelStage } from '../../types/contact.types';
import { cn } from '../../lib/utils';
import { getStatusLabel } from '../../utils/formateStatusLabel';

interface ContactStatusProps {
    contact: ContactResType;
}

const statusStyles: Record<FunnelStage, string> = {
    NEW_LEAD: "bg-accent text-white",
    CONTACTED: "bg-secondary text-white",
    IN_NEGOTIATION: "bg-primary text-white",
    PROPOSAL_SENT: "bg-primary/50 text-foreground",
    CLOSED_LOST: "bg-error text-white",
    CLOSED_WON: "bg-success text-white",
}


const ContactStatus = ({ contact }: ContactStatusProps) => {
    return (
        <div className="space-y-2.5">
            <label className="text-sm font-semibold text-foreground">
                Estado
            </label>
            <Select
                value={getStatusLabel(contact.funnelStatus)}
                onValueChange={(value) => console.log('Selected status:', value)}
            >
                <SelectTrigger
                    className={cn(
                        "transition-all",
                        statusStyles[contact.funnelStatus]
                    )}
                >
                    <SelectValue />
                </SelectTrigger>
                <SelectContent className="bg-white !border-none rounded-b-xl shadow-md p-1">
                    <SelectItem value="NEW_LEAD">
                        <div className="flex items-center gap-2">
                            <div className="h-2 w-2 rounded-full bg-accent" />
                            Nuevo
                        </div>
                    </SelectItem>
                    <SelectItem value="CONTACTED">
                        <div className="flex items-center gap-2">
                            <div className="h-2 w-2 rounded-full bg-secondary" />
                            Contactado
                        </div>
                    </SelectItem>
                    <SelectItem value="IN_NEGOTIATION">
                        <div className="flex items-center gap-2">
                            <div className="h-2 w-2 rounded-full bg-primary" />
                            En negociación
                        </div>
                    </SelectItem>
                    <SelectItem value="PROPOSAL_SENT">
                        <div className="flex items-center gap-2">
                            <div className="h-2 w-2 rounded-full bg-primary/50" />
                            Propuesta enviada
                        </div>
                    </SelectItem>
                    <SelectItem value="CLOSED_WON">
                        <div className="flex items-center gap-2">
                            <div className="h-2 w-2 rounded-full bg-success" />
                            Cliente ganado
                        </div>
                    </SelectItem>
                    <SelectItem value="CLOSED_LOST">
                        <div className="flex items-center gap-2">
                            <div className="h-2 w-2 rounded-full bg-error" />
                            Cliente perdido
                        </div>
                    </SelectItem>
                </SelectContent>
            </Select>
        </div>
    )
}

export default ContactStatus
