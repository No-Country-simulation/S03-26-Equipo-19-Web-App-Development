import { useState } from 'react';
import { Paperclip, Mic, Send, MoreVertical } from 'lucide-react';
import { useGetConversations } from '../../services/use_queries/conversations-query';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiMessagesService, apiConversationsService } from '../../services/general_api';
import type { ConversationResType } from '../../types/conversation.types';
import type { MessageResType } from '../../types/message.types';

// --- SUBCOMPONENTES ---
const StatusBadge = ({ status }: { status: string }) => {
  const isOpen = status === 'OPEN';
  return (
    <span className={`flex items-center gap-1 text-xs font-semibold ${isOpen ? 'text-green-600' : 'text-slate-400'}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${isOpen ? 'bg-green-500' : 'bg-slate-400'}`} />
      {isOpen ? 'ABIERTA' : 'CERRADA'}
    </span>
  );
};

const ChannelTag = ({ channel }: { channel: string }) => {
  const isWA = channel?.toUpperCase() === 'WHATSAPP';
  return (
    <span className={`px-2 py-0.5 rounded text-xs font-semibold ${isWA ? 'bg-green-100 text-green-700' : 'bg-blue-100 text-blue-700'}`}>
      {isWA ? 'WhatsApp' : 'Email'}
    </span>
  );
};

const MessageBubble = ({ message }: { message: MessageResType }) => {
  const isOut = message.direction === 'OUTBOUND';

  if (message.fileUrl) {
    return (
      <div className={`flex ${isOut ? 'justify-end' : 'justify-start'} mb-3`}>
        <div className={`px-4 py-2.5 rounded-2xl max-w-xs text-sm border ${isOut ? 'bg-[#13316b] text-white border-transparent' : 'bg-white border-slate-200 text-slate-700'}`}>
          <p className="text-xs font-medium mb-1">📎 Archivo adjunto</p>
          <a href={message.fileUrl} target="_blank" rel="noreferrer" className="text-xs underline opacity-80">
            Ver archivo
          </a>
          <span className="text-xs mt-1 block text-right opacity-60">
            {message.createdAt ? new Date(message.createdAt).toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' }) : ''}
          </span>
        </div>
      </div>
    );
  }

  return (
    <div className={`flex ${isOut ? 'justify-end' : 'justify-start'} mb-3`}>
      <div className={`px-4 py-2.5 rounded-2xl max-w-xs text-sm ${
        isOut ? 'bg-[#13316b] text-white rounded-br-sm' : 'bg-white border border-slate-200 text-slate-700 rounded-bl-sm'
      }`}>
        <p>{message.body}</p>
        <span className={`text-xs mt-1 block text-right ${isOut ? 'text-blue-200' : 'text-slate-400'}`}>
          {message.createdAt ? new Date(message.createdAt).toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' }) : ''}
        </span>
      </div>
    </div>
  );
};

// --- PÁGINA PRINCIPAL ---
export const Conversations = () => {
  const [channelFilter, setChannelFilter] = useState<'all' | 'WHATSAPP' | 'EMAIL'>('all');
  const [statusFilter, setStatusFilter] = useState<'Todas' | 'Abiertas' | 'Cerradas'>('Todas');
  const [activeConvId, setActiveConvId] = useState<number | null>(null);
  const [input, setInput] = useState('');
  const qc = useQueryClient();

  const { data: conversations = [], isLoading } = useGetConversations();

  const filtered = (conversations as ConversationResType[]).filter((c) => {
    const matchChannel = channelFilter === 'all' || c.channel?.toUpperCase() === channelFilter;
    const matchStatus =
      statusFilter === 'Todas' ||
      (statusFilter === 'Abiertas' && c.status === 'OPEN') ||
      (statusFilter === 'Cerradas' && c.status === 'CLOSED');
    return matchChannel && matchStatus;
  });

  const activeConv: ConversationResType | undefined =
    activeConvId != null
      ? (conversations as ConversationResType[]).find((c) => c.id === activeConvId)
      : filtered[0];

  // Mensajes de la conversación activa
  const { data: messages = [] } = useQuery<MessageResType[]>({
    queryKey: ['messages', activeConv?.id],
    queryFn: async () => {
      const res = await apiMessagesService.get(`/conversations/${activeConv!.id}/history`);
      return res.data;
    },
    enabled: !!activeConv?.id,
  });

  const sendMessage = useMutation({
    mutationFn: async (content: string) => {
      await apiMessagesService.post('/send', {
        conversationId: activeConv?.id,
        content,
        channel: activeConv?.channel,
      });
    },
    onSuccess: () => {
      setInput('');
      qc.invalidateQueries({ queryKey: ['messages', activeConv?.id] });
    },
  });

  const closeConv = useMutation({
    mutationFn: (id: number) => apiConversationsService.patch(`/${id}/close`, {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['conversations'] }),
  });

  const reopenConv = useMutation({
    mutationFn: (id: number) => apiConversationsService.patch(`/${id}/reopen`, {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['conversations'] }),
  });

  const handleSend = () => {
    if (!input.trim() || !activeConv) return;
    sendMessage.mutate(input.trim());
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-[#13316b]">Conversaciones</h1>
        <p className="text-slate-500 text-sm mt-0.5">Seguimiento del flujo de conversaciones de agentes activos</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6 h-[calc(100vh-240px)]">

        {/* Panel izquierdo */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-100 shadow-sm flex flex-col overflow-hidden">
          <div className="px-4 py-3 border-b border-slate-100">
            <p className="text-xs text-slate-400 font-medium mb-2">CANAL</p>
            <div className="flex gap-2 mb-3">
              {(['all', 'WHATSAPP', 'EMAIL'] as const).map(ch => (
                <button key={ch} onClick={() => setChannelFilter(ch)}
                  className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors ${
                    channelFilter === ch ? 'bg-[#13316b] text-white' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'
                  }`}>
                  {ch === 'all' ? 'Todos' : ch === 'WHATSAPP' ? 'WhatsApp' : 'Email'}
                </button>
              ))}
            </div>
            <p className="text-xs text-slate-400 font-medium mb-2">ESTADO</p>
            <div className="flex gap-2">
              {(['Todas', 'Abiertas', 'Cerradas'] as const).map(s => (
                <button key={s} onClick={() => setStatusFilter(s)}
                  className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors ${
                    statusFilter === s ? 'bg-[#13316b] text-white' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'
                  }`}>
                  {s}
                </button>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-4 px-4 py-2 text-xs text-slate-400 font-medium uppercase tracking-wide border-b border-slate-100">
            <span>Contacto</span>
            <span>Canal</span>
            <span>Fecha</span>
            <span>Estado</span>
          </div>

          <div className="flex-1 overflow-y-auto divide-y divide-slate-50">
            {isLoading ? (
              <div className="px-4 py-8 text-center text-slate-400 text-sm">Cargando...</div>
            ) : filtered.length === 0 ? (
              <div className="px-4 py-8 text-center text-slate-400 text-sm">No hay conversaciones</div>
            ) : (
              filtered.map((conv) => {
                const contactName = conv.contact?.name ?? '?';
                const initials = contactName.split(' ').map((n: string) => n[0]).join('').slice(0, 2).toUpperCase();
                return (
                  <button key={conv.id} onClick={() => setActiveConvId(conv.id)}
                    className={`w-full grid grid-cols-4 items-center px-4 py-3 text-left hover:bg-slate-50 transition-colors ${
                      activeConv?.id === conv.id ? 'bg-blue-50' : ''
                    }`}>
                    <div className="flex items-center gap-2">
                      <div className="w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
                        {initials}
                      </div>
                      <span className="text-xs text-slate-700 font-medium truncate">{contactName}</span>
                    </div>
                    <ChannelTag channel={conv.channel ?? ''} />
                    <p className="text-xs text-slate-500">
                      {conv.lastInteraction ? new Date(conv.lastInteraction).toLocaleDateString('es-AR') : '—'}
                    </p>
                    <StatusBadge status={conv.status ?? ''} />
                  </button>
                );
              })
            )}
          </div>
        </div>

        {/* Panel derecho — Chat */}
        <div className="lg:col-span-3 bg-white rounded-2xl border border-slate-100 shadow-sm flex flex-col overflow-hidden">
          {!activeConv ? (
            <div className="flex-1 flex items-center justify-center text-slate-400 text-sm">
              Seleccioná una conversación
            </div>
          ) : (
            <>
              <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-sm font-bold">
                    {(activeConv.contact?.name ?? '?').split(' ').map((n: string) => n[0]).join('').slice(0, 2).toUpperCase()}
                  </div>
                  <div>
                    <p className="font-semibold text-slate-800">{activeConv.contact?.name ?? '—'}</p>
                    <p className="text-xs text-slate-400">{activeConv.contact?.email ?? ''}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <ChannelTag channel={activeConv.channel ?? ''} />
                  <StatusBadge status={activeConv.status ?? ''} />
                  <button
                    className="text-xs font-semibold px-3 py-1 rounded-lg border border-slate-200 text-slate-500 hover:bg-slate-50 transition-colors"
                    onClick={() =>
                      activeConv.status === 'OPEN'
                        ? closeConv.mutate(activeConv.id)
                        : reopenConv.mutate(activeConv.id)
                    }
                  >
                    {activeConv.status === 'OPEN' ? 'Cerrar' : 'Reabrir'}
                  </button>
                  <button className="text-slate-400 hover:text-slate-600"><MoreVertical size={18} /></button>
                </div>
              </div>

              <div className="flex-1 overflow-y-auto px-5 py-4 bg-slate-50">
                {(messages as MessageResType[]).length === 0 ? (
                  <div className="text-center text-slate-400 text-sm mt-8">No hay mensajes aún</div>
                ) : (
                  (messages as MessageResType[]).map((msg) => (
                    <MessageBubble key={msg.id} message={msg} />
                  ))
                )}
              </div>

              <div className="px-4 py-3 border-t border-slate-100 flex items-center gap-3">
                <button className="text-slate-400 hover:text-slate-600"><Paperclip size={18} /></button>
                <input
                  type="text"
                  placeholder="Escribe un mensaje..."
                  value={input}
                  onChange={e => setInput(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && handleSend()}
                  className="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2 text-sm text-slate-700 placeholder:text-slate-400 outline-none focus:border-blue-400 transition-colors"
                />
                <button className="text-slate-400 hover:text-slate-600"><Mic size={18} /></button>
                <button
                  onClick={handleSend}
                  disabled={sendMessage.isPending}
                  className="w-9 h-9 bg-[#13316b] rounded-xl flex items-center justify-center text-white hover:bg-[#0f2557] transition-colors disabled:opacity-50"
                >
                  <Send size={16} />
                </button>
              </div>
            </>
          )}
        </div>
      </div>

      <div className="mt-6 pt-4 border-t border-slate-200 flex flex-col sm:flex-row justify-between items-center gap-2 text-xs text-slate-400">
        <span>© 2026 Conversa CRM. Todos los derechos reservados.</span>
        <div className="flex gap-4">
          <button className="hover:text-slate-600 transition-colors">Política de Privacidad</button>
          <button className="hover:text-slate-600 transition-colors">Términos y Condiciones de Uso</button>
          <button className="hover:text-slate-600 transition-colors">Preguntas Frecuentes</button>
        </div>
      </div>
    </div>
  );
};