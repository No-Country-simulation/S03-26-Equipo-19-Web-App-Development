import { Eye, Mail, MessageCircleMore, UserRoundPlus } from "lucide-react"
import type { ConversationResType } from "../../types/conversation.types"
import { formatDateTime } from "../../utils/formateDate"
import { useState } from "react"

import type { ContactResType } from "../../types/contact.types"


interface ConversationHistoryProps {
    contact: ContactResType
    conversations: ConversationResType[]
}

const ConversationHistory = ({ contact, conversations }: ConversationHistoryProps) => {


    const [modalOpen, setModalOpen] = useState(false);

    return (
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
                                    <td className="py-4 px-2">{formatDateTime(conversation?.createdAt)}hs</td>

                                    <td className="py-2 px-2 flex justify-end gap-3">

                                        <button
                                            aria-label={`Ver ${conversation?.id}`}
                                            title="Ver conversación"
                                            className={`p-1 rounded-md text-primary hover:bg-secondary hover:text-white transition-colors flex items-center justify-center`}
                                            onClick={() => setModalOpen(true)}
                                        >
                                            <Eye size={24} />
                                        </button>
                                    </td>
                                </tr>
                            )))}
                    </tbody>
                </table>
            </div>
        </div>
    )
}

export default ConversationHistory
