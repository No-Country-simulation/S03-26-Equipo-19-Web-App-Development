import { useParams } from "react-router-dom"
import { useGetContactById } from "../services/use_queries/contacts-query";
import { ContactHeader } from "../components/contacts/ContactHeader";
import type { Channel } from "../types/contact.types";
import { useMemo, useState } from "react";
import { ConversationPanel } from "../components/contacts/ConversationPanel";
import { ContactCard } from "../components/contacts/ContactCard";
import { ScrollArea } from "../components/ui/scroll-area";
import { TaskList } from "../components/contacts/TasksList";
import { mockMessages, mockTasks } from "../constants/mocks.ts";
import ContactTags from "../components/contacts/ContactTags.tsx";
import ContactStatus from "../components/contacts/ContactStatus.tsx";
import { MessageInput } from "../components/contacts/MessageInput.tsx";
import ConversationHistory from "../components/contacts/ConversationHistory.tsx";
import { useGetConversationsByContactId } from "../services/use_queries/conversations-query.ts";



const ContactDetailPage = () => {
    const { contactId } = useParams<{ contactId: string }>()
    const [activeChannel, setActiveChannel] = useState<Channel>("WHATSAPP")
    const parsedId = contactId ? Number(contactId) : undefined;

    const { data: contactData, isLoading } = useGetContactById(parsedId!);
    const { data: conversationsData } = useGetConversationsByContactId(parsedId!);

    console.log({activeChannel});
    

    const messages = useMemo(() => {
        return mockMessages.filter(m => m.channel === activeChannel)
    }, [activeChannel])


    const channelCounts = useMemo(() => {
        return {
            WHATSAPP: messages.filter((m) => m.channel === "WHATSAPP").length,
            EMAIL: messages.filter((m) => m.channel === "EMAIL").length,
        }
    }, [messages])


    if (isLoading) return <div className="flex items-center justify-center">
        <p className="text-lg font-medium text-primary">Cargando datos de contacto...</p>
    </div>;


    return (
        <>
            {contactData ? (
                <>
                    <div className="grid grid-cols-1 md:grid-cols-[3fr_1fr] lg:grid-cols-[2fr_1fr] gap-4">
                        <div className="flex flex-col gap-4">
                            <div className=" bg-white rounded-lg shadow">
                                <ContactHeader contact={contactData} channelCounts={channelCounts} activeChannel={activeChannel} setActiveChannel={setActiveChannel} />
                                <ConversationPanel conversations={conversationsData ?? []} activeChannel={activeChannel} />
                            </div>
                            <MessageInput contact={contactData} activeChannel={activeChannel} />
                        </div>
                        <ScrollArea >
                            <div className="space-y-6">
                                <ContactCard contact={contactData} />
                                <ContactStatus contact={contactData} />
                                <ContactTags contact={contactData} />
                                <TaskList tasks={mockTasks} />
                                <ConversationHistory contact={contactData} conversations={conversationsData ?? []} />
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
