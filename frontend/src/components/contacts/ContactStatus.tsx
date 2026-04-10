import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select'
import type { ContactResType, FunnelStage } from '../../types/contact.types';
import { cn } from '../../lib/utils';
import { getStatusLabel } from '../../utils/formateStatusLabel';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateFunnelStatusByContactId } from '../../services/use_cases/contacts-service';

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

    const queryClient = useQueryClient();

    const mutationUpdateFunnelStatusById = useMutation({
        mutationFn: ({ id, status } : {id:number, status: string}) =>
            updateFunnelStatusByContactId(id, status),

        onMutate: async ({ id, status }) => {
            await queryClient.cancelQueries({ queryKey: ["contact", id] });

            const previous = queryClient.getQueryData(["contact", id]);

            queryClient.setQueryData(["contact", id], (old: any) => {
                if (!old) return old;

                return {
                    ...old,
                    funnelStatus: status, 
                };
            });

            return { previous };
        },

        onError: (_err, variables, context) => {
            queryClient.setQueryData(
                ["contact", variables.id],
                context?.previous
            );
        },

        onSettled: (_data, _err, variables) => {
            queryClient.invalidateQueries({
                queryKey: ["contact", variables.id],
            });
        },
    });


  const handleChange = (value: FunnelStage | null) => {
    if (!value) return; 
    mutationUpdateFunnelStatusById.mutate({
        id: contact.id,
        status: value
    });
};


    return (
        <div className="space-y-2.5">
            <label className="text-sm font-semibold text-foreground">
                Estado
            </label>

            <Select
                value={contact.funnelStatus}
                onValueChange={handleChange}
            >
                <SelectTrigger
                    className={cn(
                        "transition-all",
                        statusStyles[contact.funnelStatus]
                    )}
                >
                    <SelectValue>
                        {getStatusLabel(contact.funnelStatus)}
                    </SelectValue>
                </SelectTrigger>

                <SelectContent className="bg-white !border-none rounded-b-xl shadow-md p-1">

                    <SelectItem value="NEW_LEAD">Nuevo</SelectItem>
                    <SelectItem value="CONTACTED">Contactado</SelectItem>
                    <SelectItem value="IN_NEGOTIATION">En negociación</SelectItem>
                    <SelectItem value="PROPOSAL_SENT">Propuesta enviada</SelectItem>
                    <SelectItem value="CLOSED_WON">Cliente ganado</SelectItem>
                    <SelectItem value="CLOSED_LOST">Cliente perdido</SelectItem>

                </SelectContent>
            </Select>
        </div>
    )
}

export default ContactStatus;