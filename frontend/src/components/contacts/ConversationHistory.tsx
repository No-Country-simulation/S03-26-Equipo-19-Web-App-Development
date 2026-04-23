import { Eye, LockKeyhole, LockOpen, Mail, MessageCircleMore } from "lucide-react"
import type { ConversationResType } from "../../types/conversation.types"
import { formatDateTime } from "../../utils/formateDate"
import { useState } from "react"
import { Modal } from "../ui/Modal"
import { ConversationPanel } from "./ConversationPanel"
import { useGetMessagesByConversationId } from "../../services/use_queries/messages-query"
import { useConversationsMutationsService } from "../../services/use_mutations/conversations-mutation"


interface ConversationHistoryProps {
    conversations: ConversationResType[]
}

const ConversationHistory = ({conversations }: ConversationHistoryProps) => {


    const [modalOpen, setModalOpen] = useState(false);
    const [selectedConversation, setSelectedConversation] = useState<ConversationResType | null>(null);

    const { data: messages = [], isLoading } = useGetMessagesByConversationId(selectedConversation?.id);

    const { mutationReopenConversationById, mutationCloseConversationById } = useConversationsMutationsService();

    const toggleOpenCloseConversation = (conversation: ConversationResType) => {
           
        if (conversation.status === 'OPEN') {
           mutationCloseConversationById.mutate({ id: conversation.id });
        } else {
            mutationReopenConversationById.mutate({ id: conversation.id });
        }
    };


    return (
        <>
            <div className="bg-white rounded-lg shadow-md p-4">
                <p className="font-bold">Historial de conversaciones</p>
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse text-xs">
                        <thead>
                            <tr className="border-b border-neutro-2 font-semibold text-neutro-2">
                                <th className="py-3 px-1">Canal</th>
                                <th className="py-3 px-1">Estado</th>
                                <th className="py-3 px-1">Última interacción</th>
                                <th className="py-3 px-1 text-right">Acción</th>
                            </tr>
                        </thead>
                        <tbody>
                            {conversations.length === 0 ? (
                                <tr>
                                    <td colSpan={4} className="text-center py-4 text-gray-400">
                                        No hay conversaciones
                                    </td>
                                </tr>
                            ) : (
                                conversations?.map((conversation: ConversationResType) => (
                                    <tr key={conversation.id} className="border-b border-slate-50 last:border-0 hover:bg-slate-50 transition-colors text-slate-600">
                                        <td className="py-4 px-1 font-medium text-neutro-1 flex items-center gap-3 min-w-0">
                                            <span className="flex items-center gap-1">
                                                {conversation?.channel === 'WHATSAPP' ? <MessageCircleMore size={24} className='text-success' /> : <Mail size={24} className='text-primary' />}
                                                {conversation?.channel === 'WHATSAPP' ? 'WhatsApp' : 'Email'}
                                            </span>

                                        </td>
                                        <td className="py-4 px-2">
                                            <p className="text-sm font-medium text-foreground line-clamp-1">
                                                {conversation?.status || 'Sin mensajes'}
                                            </p>
                                        </td>
                                        <td className="py-4 px-2">{formatDateTime(conversation?.lastInteraction)}hs</td>

                                        <td className="py-2 px-2 flex justify-end gap-2">

                                            <button
                                                aria-label={`Ver ${conversation?.id}`}
                                                title="Ver conversación"
                                                className={`p-1 rounded-md text-primary hover:bg-secondary hover:text-white transition-colors flex items-center justify-center`}
                                                onClick={() => {
                                                    setSelectedConversation(conversation);
                                                    setModalOpen(true);
                                                }}
                                            >
                                                <Eye size={24} />
                                            </button>
                                            <button
                                                aria-label={conversation.status === 'OPEN' ? `Cerrar ${conversation?.id}` : `Reabrir ${conversation?.id}`}
                                                title={conversation.status === 'OPEN' ? 'Cerrar conversación' : 'Reabrir conversación'}
                                                className={`p-1 rounded-md text-primary hover:bg-secondary hover:text-white transition-colors flex items-center justify-center`}
                                                onClick={() => { toggleOpenCloseConversation(conversation) }}
                                            >
                                                {conversation.status === 'OPEN' ? <LockKeyhole size={24} /> : <LockOpen size={24} />}
                                            </button>

                                        </td>
                                    </tr>
                                )))}
                        </tbody>
                    </table>
                </div>
            </div>

            <Modal
                isOpen={modalOpen}
                onClose={() => setModalOpen(false)}
                title="Detalles de la conversación"
            >
                <p>Canal: {selectedConversation?.channel} </p>
                <span className="text-sm text-gray-500">Última interacción: {formatDateTime(selectedConversation?.lastInteraction!)}hs</span>

                <ConversationPanel
                    messages={messages}
                    activeConversation={!!selectedConversation}
                    isLoading={isLoading}
                />
            </Modal>

        </>
    )
}

export default ConversationHistory
