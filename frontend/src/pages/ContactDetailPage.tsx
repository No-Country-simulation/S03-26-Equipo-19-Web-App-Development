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



const ContactDetailPage = () => {
    const { contactId } = useParams<{ contactId: string }>()
    const [activeChannel, setActiveChannel] = useState<Channel>("whatsapp")
    const parsedId = Number(contactId)

    const { data: contactData, isLoading } = useGetContactById(parsedId);

    const messages = useMemo(() => {
        return mockMessages.filter(m => m.channel === activeChannel)
    }, [activeChannel])


    const channelCounts = useMemo(() => {
        return {
            whatsapp: messages.filter((m) => m.channel === "whatsapp").length,
            email: messages.filter((m) => m.channel === "email").length,
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
                                <ConversationPanel messages={messages} activeChannel={activeChannel} />
                            </div>
                                <MessageInput contact={contactData} activeChannel={activeChannel} />
                        </div>
                        <ScrollArea >
                            <div className="space-y-6">
                                <ContactCard contact={contactData} />
                                <ContactStatus contact={contactData} />
                                <ContactTags contact={contactData} />
                                <TaskList tasks={mockTasks} />
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
