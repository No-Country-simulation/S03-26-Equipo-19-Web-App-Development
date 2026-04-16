import { SlidersHorizontal, MessageCircleMore, Mail, Eye } from 'lucide-react';
import { formatDateTime } from '../../utils/formateDate';
import { useGetConversationsInbox } from '../../services/use_queries/conversations-query';
import type { ConversationInboxItemType } from '../../types/conversation.types';
import { ROUTES } from '../../constants/routes';
import { useNavigate } from 'react-router-dom';
import { Button } from '../ui/Button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '../ui/dropdown-menu';
import { useMemo, useState } from 'react';

export const InboxTable = () => {
  const { data: conversationsInbox, isLoading } = useGetConversationsInbox();
  const navigate = useNavigate();

  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [channelFilter, setChannelFilter] = useState<'ALL' | 'WHATSAPP' | 'EMAIL'>('ALL');

  const filteredConversations = useMemo(() => {
    if (!conversationsInbox) return [];
    let result = [...conversationsInbox];

    if (channelFilter !== 'ALL') {
      result = result.filter(
        (conv) => conv.channel === channelFilter
      );
    }

    result.sort((a, b) => {
      const dateA = new Date(a.lastMessageAt).getTime();
      const dateB = new Date(b.lastMessageAt).getTime();

      return sortOrder === 'desc' ? dateB - dateA : dateA - dateB;
    });

    return result;
  }, [conversationsInbox, sortOrder, channelFilter]);

  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm h-full flex flex-col">
      
      <div className='flex justify-between'>
        <h3 className="text-lg font-bold text-primary mb-4">
          Bandeja de entrada
        </h3>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button className="p-2 rounded-md hover:bg-slate-100 transition">
              <SlidersHorizontal size={20} className="text-primary" />
            </button>
          </DropdownMenuTrigger>

          <DropdownMenuContent className="w-56 bg-white">
            
            <DropdownMenuLabel>Orden</DropdownMenuLabel>
            <DropdownMenuItem onClick={() => setSortOrder("desc")}>
              {sortOrder === "desc" && "✓ "} Más recientes
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setSortOrder("asc")}>
              {sortOrder === "asc" && "✓ "} Más antiguos
            </DropdownMenuItem>

            <DropdownMenuSeparator />

            <DropdownMenuLabel>Canal</DropdownMenuLabel>
            <DropdownMenuItem onClick={() => setChannelFilter("ALL")}>
              {channelFilter === "ALL" && "✓ "} Todos
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setChannelFilter("WHATSAPP")}>
              {channelFilter === "WHATSAPP" && "✓ "} Whatsapp
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setChannelFilter("EMAIL")}>
              {channelFilter === "EMAIL" && "✓ "} Email
            </DropdownMenuItem>

          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse text-xs">
          <thead>
            <tr className="border-b border-neutro-2 font-semibold text-neutro-2">
              <th className="py-3 px-1">Canal</th>
              <th className="py-3 px-1">Mensaje</th>
              <th className="py-3 px-1">Contacto</th>
              <th className="py-3 px-1">Email / Teléfono</th>
              <th className="py-3 px-1">Fecha y Hora</th>
              <th className="py-3 px-1 text-right">Acciones</th>
            </tr>
          </thead>

          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={6} className="text-center py-6">
                  <p className="text-sm font-medium text-primary animate-pulse">
                    Cargando mensajes...
                  </p>
                </td>
              </tr>
            ) : filteredConversations.length === 0 ? (
              <tr>
                <td colSpan={6} className="text-center py-6 text-neutro-2">
                  No hay conversaciones
                </td>
              </tr>
            ) : (
              filteredConversations.map((conversation: ConversationInboxItemType) => (
                <tr
                  key={conversation.conversationId}
                  className="border-b border-slate-50 last:border-0 hover:bg-slate-50 transition-colors text-slate-600"
                >
                  <td className="py-4 px-1 flex items-center">
                    {conversation.channel === 'WHATSAPP' ? (
                      <MessageCircleMore size={24} className='text-success' />
                    ) : (
                      <Mail size={24} className='text-primary' />
                    )}
                  </td>

                  <td className="py-4 px-1">
                    {conversation.lastMessagePreview}
                  </td>

                  <td className="py-4 px-2">
                    {conversation.contactName ?? "Sin datos"}
                  </td>

                  <td className="py-4 px-2">
                    {conversation.contactIdentifier ?? "Sin datos"}
                  </td>

                  <td className="py-4 px-2">
                    {formatDateTime(conversation.lastMessageAt)}hs
                  </td>

                  <td className="py-2 px-2 flex justify-end">
                    <button
                      title="Ver conversación"
                      className="p-1 rounded-md text-primary hover:bg-secondary hover:text-white transition"
                      onClick={() =>
                        navigate(`/dashboard/${ROUTES.CONTACTS}/${conversation.contactId}`)
                      }
                    >
                      <Eye size={24} />
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <Button
        variant='ghost'
        className="w-1/4 mt-4 self-end"
        onClick={() => navigate(`/dashboard/${ROUTES.MESSAGES}`)}
      >
        Ver todo
      </Button>
    </div>
  );
};