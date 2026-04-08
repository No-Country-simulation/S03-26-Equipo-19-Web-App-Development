import { useState } from 'react';
import { Paperclip, Mic, Send, MoreVertical, Download } from 'lucide-react';

// --- TIPOS ---
type ConversationStatus = 'ABIERTA' | 'CERRADA';
type Channel = 'whatsapp' | 'email';
type MessageDirection = 'inbound' | 'outbound';

interface Conversation {
  id: number;
  contact: string;
  avatar: string;
  vendor: string;
  vendorAvatar: string;
  date: string;
  time: string;
  status: ConversationStatus;
  channel: Channel;
}

interface Message {
  id: number;
  content: string;
  direction: MessageDirection;
  time: string;
  isFile?: boolean;
  fileName?: string;
}

// --- MOCK DATA ---
const mockConversations: Conversation[] = [
  { id: 1, contact: 'Anya Marcus', avatar: 'AM', vendor: 'Julianne Moore', vendorAvatar: 'JM', date: 'Hace 2 minutos', time: 'Hoy, 10:45 a.m.', status: 'ABIERTA', channel: 'whatsapp' },
  { id: 2, contact: 'Robert Tils', avatar: 'RT', vendor: 'Miguel Mateo', vendorAvatar: 'MM', date: 'Hace 45 minutos', time: 'Hoy, 9:02 am', status: 'CERRADA', channel: 'email' },
  { id: 3, contact: 'Lydia West', avatar: 'LW', vendor: 'Julian Álvarez', vendorAvatar: 'JA', date: 'Hace 2 horas', time: 'Hoy, 8:31 a.m.', status: 'ABIERTA', channel: 'whatsapp' },
  { id: 4, contact: 'Kevin G.', avatar: 'KG', vendor: 'Pedro Fernández', vendorAvatar: 'PF', date: 'ayer', time: '24-03-2025, 14:11', status: 'CERRADA', channel: 'email' },
  { id: 5, contact: 'Kevin G.', avatar: 'KG', vendor: 'Marcos Sosa', vendorAvatar: 'MS', date: 'ayer', time: '24-03-2025, 10:32', status: 'CERRADA', channel: 'whatsapp' },
];

const mockMessages: Message[] = [
  { id: 1, content: 'Hola, ¿cómo estás? Tengo unas promos para vos.', direction: 'inbound', time: '10:12 PM' },
  { id: 2, content: 'Hola, bienvenido, pasemos.', direction: 'outbound', time: '10:14 PM' },
  { id: 3, content: 'Dale, te envío el archivo.', direction: 'outbound', time: '10:16 PM', isFile: true, fileName: 'promociones_marzo.csv' },
];

// --- SUBCOMPONENTES ---
const StatusBadge = ({ status }: { status: ConversationStatus }) => (
  <span className={`flex items-center gap-1 text-xs font-semibold ${status === 'ABIERTA' ? 'text-green-600' : 'text-slate-400'}`}>
    <span className={`w-1.5 h-1.5 rounded-full ${status === 'ABIERTA' ? 'bg-green-500' : 'bg-slate-400'}`} />
    {status}
  </span>
);

const ChannelTag = ({ channel }: { channel: Channel }) => (
  <span className={`px-2 py-0.5 rounded text-xs font-semibold ${channel === 'whatsapp' ? 'bg-green-100 text-green-700' : 'bg-blue-100 text-blue-700'}`}>
    {channel === 'whatsapp' ? 'WhatsApp' : 'Email'}
  </span>
);

const MessageBubble = ({ message }: { message: Message }) => {
  const isOut = message.direction === 'outbound';

  if (message.isFile) {
    return (
      <div className={`flex ${isOut ? 'justify-end' : 'justify-start'} mb-3`}>
        <div className="bg-slate-100 rounded-2xl px-4 py-3 max-w-xs">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
              <Paperclip size={14} className="text-blue-600" />
            </div>
            <span className="text-sm text-slate-700 font-medium">{message.fileName}</span>
            <button className="text-slate-400 hover:text-slate-600">
              <Download size={14} />
            </button>
          </div>
          <p className="text-xs text-slate-500 mt-2">{message.content}</p>
          <span className="text-xs text-slate-400 mt-1 block text-right">{message.time}</span>
        </div>
      </div>
    );
  }

  return (
    <div className={`flex ${isOut ? 'justify-end' : 'justify-start'} mb-3`}>
      <div className={`px-4 py-2.5 rounded-2xl max-w-xs text-sm ${
        isOut
          ? 'bg-[#13316b] text-white rounded-br-sm'
          : 'bg-white border border-slate-200 text-slate-700 rounded-bl-sm'
      }`}>
        <p>{message.content}</p>
        <span className={`text-xs mt-1 block text-right ${isOut ? 'text-blue-200' : 'text-slate-400'}`}>
          {message.time}
        </span>
      </div>
    </div>
  );
};

// --- PÁGINA PRINCIPAL ---
export const Conversations = () => {
  const [activeConv, setActiveConv] = useState<Conversation>(mockConversations[0]);
  const [channelFilter, setChannelFilter] = useState<'all' | 'whatsapp' | 'email'>('all');
  const [statusFilter, setStatusFilter] = useState<'Todas' | 'Abiertas' | 'Cerradas'>('Todas');
  const [input, setInput] = useState('');

  const filtered = mockConversations.filter(c => {
    const matchChannel = channelFilter === 'all' || c.channel === channelFilter;
    const matchStatus =
      statusFilter === 'Todas' ||
      (statusFilter === 'Abiertas' && c.status === 'ABIERTA') ||
      (statusFilter === 'Cerradas' && c.status === 'CERRADA');
    return matchChannel && matchStatus;
  });

  return (
    <div>
      {/* Encabezado */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-[#13316b]">Conversaciones</h1>
        <p className="text-slate-500 text-sm mt-0.5">Seguimiento del flujo de conversaciones de agentes activos</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6 h-[calc(100vh-240px)]">

        {/* Panel izquierdo */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-100 shadow-sm flex flex-col overflow-hidden">

          {/* Filtros */}
          <div className="px-4 py-3 border-b border-slate-100">
            {/* Canal */}
            <p className="text-xs text-slate-400 font-medium mb-2">CANAL</p>
            <div className="flex gap-2 mb-3">
              {(['all', 'whatsapp', 'email'] as const).map(ch => (
                <button
                  key={ch}
                  onClick={() => setChannelFilter(ch)}
                  className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors ${
                    channelFilter === ch
                      ? 'bg-[#13316b] text-white'
                      : 'bg-slate-100 text-slate-500 hover:bg-slate-200'
                  }`}
                >
                  {ch === 'all' ? 'Todos' : ch === 'whatsapp' ? 'WhatsApp' : 'Email'}
                </button>
              ))}
            </div>

            {/* Estado */}
            <p className="text-xs text-slate-400 font-medium mb-2">ESTADO</p>
            <div className="flex gap-2">
              {(['Todas', 'Abiertas', 'Cerradas'] as const).map(s => (
                <button
                  key={s}
                  onClick={() => setStatusFilter(s)}
                  className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors ${
                    statusFilter === s
                      ? 'bg-[#13316b] text-white'
                      : 'bg-slate-100 text-slate-500 hover:bg-slate-200'
                  }`}
                >
                  {s}
                </button>
              ))}
            </div>
          </div>

          {/* Headers tabla */}
          <div className="grid grid-cols-4 px-4 py-2 text-xs text-slate-400 font-medium uppercase tracking-wide border-b border-slate-100">
            <span>Contacto</span>
            <span>Vendedor</span>
            <span>Fecha</span>
            <span>Estado</span>
          </div>

          {/* Lista conversaciones */}
          <div className="flex-1 overflow-y-auto divide-y divide-slate-50">
            {filtered.map(conv => (
              <button
                key={conv.id}
                onClick={() => setActiveConv(conv)}
                className={`w-full grid grid-cols-4 items-center px-4 py-3 text-left hover:bg-slate-50 transition-colors ${
                  activeConv.id === conv.id ? 'bg-blue-50' : ''
                }`}
              >
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
                    {conv.avatar}
                  </div>
                  <span className="text-xs text-slate-700 font-medium truncate">{conv.contact}</span>
                </div>
                <div className="flex items-center gap-1">
                  <div className="w-6 h-6 rounded-full bg-slate-200 flex items-center justify-center text-xs font-bold text-slate-600 shrink-0">
                    {conv.vendorAvatar.charAt(0)}
                  </div>
                  <span className="text-xs text-slate-500 truncate">{conv.vendor}</span>
                </div>
                <div>
                  <p className="text-xs text-slate-500">{conv.date}</p>
                  <p className="text-xs text-slate-400">{conv.time}</p>
                </div>
                <StatusBadge status={conv.status} />
              </button>
            ))}
          </div>
        </div>

        {/* Panel derecho — Chat */}
        <div className="lg:col-span-3 bg-white rounded-2xl border border-slate-100 shadow-sm flex flex-col overflow-hidden">

          {/* Header chat */}
          <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-sm font-bold">
                {activeConv.avatar}
              </div>
              <div>
                <p className="font-semibold text-slate-800">{activeConv.contact}</p>
                <p className="text-xs text-slate-400">Contacto</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <ChannelTag channel={activeConv.channel} />
              <span className="text-xs text-slate-500 font-medium">{activeConv.vendor}</span>
              <button className="text-slate-400 hover:text-slate-600">
                <MoreVertical size={18} />
              </button>
            </div>
          </div>

          {/* Mensajes */}
          <div className="flex-1 overflow-y-auto px-5 py-4 bg-slate-50">
            {mockMessages.map(msg => (
              <MessageBubble key={msg.id} message={msg} />
            ))}
            <div className="text-center my-3">
              <span className="text-xs text-slate-400 bg-slate-200 px-3 py-1 rounded-full">HOY</span>
            </div>
          </div>

          {/* Input */}
          <div className="px-4 py-3 border-t border-slate-100 flex items-center gap-3">
            <button className="text-slate-400 hover:text-slate-600">
              <Paperclip size={18} />
            </button>
            <input
              type="text"
              placeholder="Escribe un mensaje..."
              value={input}
              onChange={e => setInput(e.target.value)}
              className="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2 text-sm text-slate-700 placeholder:text-slate-400 outline-none focus:border-blue-400 transition-colors"
            />
            <button className="text-slate-400 hover:text-slate-600">
              <Mic size={18} />
            </button>
            <button className="w-9 h-9 bg-[#13316b] rounded-xl flex items-center justify-center text-white hover:bg-[#0f2557] transition-colors">
              <Send size={16} />
            </button>
          </div>
        </div>
      </div>

      {/* Footer */}
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