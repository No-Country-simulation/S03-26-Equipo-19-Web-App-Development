import { useParams } from "react-router-dom"
import { useGetContactById } from "../services/use_queries/contacts-query";
import { ContactCard } from "../components/contacts/ContactCard";
import { ScrollArea } from "../components/ui/scroll-area";
import { TaskList } from "../components/contacts/TasksList";
import ContactTags from "../components/contacts/ContactTags.tsx";
import ContactStatus from "../components/contacts/ContactStatus.tsx";
import ConversationHistory from "../components/contacts/ConversationHistory.tsx";
import { useGetConversationsByContactId } from "../services/use_queries/conversations-query.ts";
import MessagePanel from "../components/messages/MessagePanel.tsx";
import { ContactsMutationsService } from "../services/use_mutations/contacts-mutation.ts";


const ContactDetailPage = () => {
    const { contactId } = useParams<{ contactId: string }>()
    const parsedId = contactId ? Number(contactId) : undefined;

    const { data: contactData, isLoading } = useGetContactById(parsedId!);
    const { data: conversationsData = [] } = useGetConversationsByContactId(parsedId!);
    const { mutationAddTagByContactId, mutationRemoveTagFromContactId } = ContactsMutationsService();


    const handleAddTag = (tagId: number) => {
        if (parsedId !== undefined){
            mutationAddTagByContactId.mutate({ contactId: parsedId, tagId });
        }   
    };

    const handleRemoveTag = (tagId: number) => {
        if (parsedId !== undefined) {
            mutationRemoveTagFromContactId.mutate({ contactId: parsedId, tagId });
        }
    };
  

    if (isLoading) return <div className="flex items-center justify-center">
        <p className="text-lg font-medium text-primary">Cargando datos de contacto...</p>
    </div>;


    return (
        <>
            {contactData ? (
                <>
                    <div className="grid grid-cols-1 md:grid-cols-[3fr_1fr] lg:grid-cols-[2fr_1fr] gap-4">
                        <MessagePanel contact={contactData} conversations={conversationsData}/>
                        <ScrollArea className={"lg:h-screen"}>
                            <div className="space-y-6">
                                <ContactCard contact={contactData} />
                                <ContactStatus contact={contactData} />
                                <ContactTags contact={contactData} onAddTag={handleAddTag} onRemoveTag={handleRemoveTag} />
                                <TaskList contactId={contactData.id}/>
                                <ConversationHistory conversations={conversationsData ?? []} />
                            </div>
                        </ScrollArea>
                    </div>
                </>
            ) : (
                <div className="flex items-center justify-center">
                    <p className="text-lg font-medium text-primary">Contacto no encontrado</p>
                </div>
            )}
        </>
    )
}

export default ContactDetailPage
