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
import { useEffect, useState } from "react";
import { Modal } from "../components/ui/Modal.tsx";
import { ContactForm } from "../components/contacts/ContactForm.tsx";
import type { ContactReqType, ContactResType } from "../types/contact.types.ts";


const ContactDetailPage = () => {
    const { contactId } = useParams<{ contactId: string }>()
    const parsedId = contactId ? Number(contactId) : undefined;
    const [isModalOpen, setIsModalOpen] = useState(false);

    const { data: contactData, isLoading } = useGetContactById(parsedId!);
    const { data: conversationsData = [] } = useGetConversationsByContactId(parsedId!);
    const { mutationAddTagByContactId, mutationRemoveTagFromContactId } = ContactsMutationsService();

    const [selectedContact, setSelectedContact] = useState<ContactResType | null>(null);

    const { mutationUpdateContactById } = ContactsMutationsService();

    const updateContact = (data: ContactReqType) => {
        if (!selectedContact) return;

        mutationUpdateContactById.mutate(
            { id: selectedContact.id, data },
            {
                onSuccess: () => {
                    setIsModalOpen(false);
                    setSelectedContact(null);
                },
            }
        );
    };
    const handleAddTag = (tagId: number) => {
        if (parsedId !== undefined) {
            mutationAddTagByContactId.mutate({ contactId: parsedId, tagId });
        }
    };

    const handleRemoveTag = (tagId: number) => {
        if (parsedId !== undefined) {
            mutationRemoveTagFromContactId.mutate({ contactId: parsedId, tagId });
        }
    };


    useEffect(() => {
        if (contactData) {
            setSelectedContact(contactData);
        }
    }, [contactData]);


    if (isLoading) return <div className="flex items-center justify-center">
        <p className="text-lg font-medium text-primary">Cargando datos de contacto...</p>
    </div>;


    return (
        <>
            {contactData ? (
                <div className="grid grid-cols-1 md:grid-cols-[1fr_1fr] lg:grid-cols-[2fr_1fr] gap-4">
                    <MessagePanel contact={contactData} conversations={conversationsData} />
                    <ScrollArea className={"lg:h-screen"}>
                        <div className="space-y-6">
                            <ContactCard contact={contactData} openModal={() => setIsModalOpen(true)} />
                            <ContactStatus contact={contactData} />
                            <ContactTags contact={contactData} onAddTag={handleAddTag} onRemoveTag={handleRemoveTag} />
                            <TaskList contactId={contactData.id} />
                            <ConversationHistory conversations={conversationsData ?? []} />
                        </div>
                    </ScrollArea>
                </div>
            ) : (
                <div className="flex items-center justify-center">
                    <p className="text-lg font-medium text-primary">Contacto no encontrado</p>
                </div>
            )}
            <Modal
                isOpen={isModalOpen}
                onClose={() => {
                    setIsModalOpen(false);
                    setSelectedContact(null);
                }}
                title="Editar contacto"
            >
                {selectedContact && (
                    <ContactForm
                        initialData={{
                            contact: {
                                name: selectedContact.name,
                                lastName: selectedContact.lastName,
                                email: selectedContact.email,
                                phone: selectedContact.phone,
                                company: selectedContact.company,
                            },
                            preferredChannel: selectedContact.preferredChannel,
                        }}
                        onSubmit={updateContact}
                        onCancel={() => {
                            setIsModalOpen(false);
                            setSelectedContact(null);
                        }}
                    />
                )}
            </Modal>
        </>
    )
}

export default ContactDetailPage
