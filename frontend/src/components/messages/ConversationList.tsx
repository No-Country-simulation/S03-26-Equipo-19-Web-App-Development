import type { ContactResType } from '../../types/contact.types';
import AvatarContact from '../ui/AvatarContact';
import TitleSection from '../ui/TitleSection';
import { useState } from 'react';
import { Tabs, TabsList, TabsTrigger } from '../ui/tabs';
import { Badge } from '../ui/Badge';
import { ScrollArea } from '../ui/scroll-area';
import { Mail, MessageCircle } from "lucide-react";
import type { ConversationResType } from '../../types/conversation.types';
import { getStatusLabel } from '../../utils/formateStatusLabel';

interface ConversationListProps {
  contacts: ContactResType[];
  activeChatId: number | null;
  onSelect: (contact: ContactResType, conversations: ConversationResType[]) => void;
}

export const ConversationList = ({
  contacts,
  activeChatId,
  onSelect
}: ConversationListProps) => {

  const [activeStatus, setActiveStatus] = useState<'ALL' | 'UNREAD'>('ALL');


  const total = contacts.length;
  const unread = contacts.filter(c => (c.totalUnreadCount || 0) > 0).length;

  return (
    <div className="relative">

      <div className='bg-white rounded-lg shadow-lg pt-8 pb-6 px-6 z-10 relative'>
        <TitleSection text="Mis Conversaciones" className='hidden md:flex' />

        <Tabs value={activeStatus} onValueChange={(v) => setActiveStatus(v as any)}>
          <TabsList className="bg-neutro-2/30 rounded-2xl lg:mt-4">

            <TabsTrigger value="ALL">
              Total
              <Badge variant="outline" className="ml-2">{total}</Badge>
            </TabsTrigger>

            <TabsTrigger value="UNREAD">
              No leídos
              <Badge variant="outline" className="ml-2">{unread}</Badge>
            </TabsTrigger>

          </TabsList>
        </Tabs>
      </div>

  
      <ScrollArea className="h-[300px]">
        <div className="flex flex-col bg-white -mt-4 pt-5">

          {contacts
            .filter(c =>
              activeStatus === 'UNREAD'
                ? (c.totalUnreadCount || 0) > 0
                : true
            )
            .map(contact => {

              const openConversations = contact.conversations?.filter(
                c => c.status === "OPEN"
              ) || [];

              const channels = [
                ...new Set(openConversations.map(c => c.channel))
              ];

              return (
                <button
                  key={contact.id}
                  onClick={() => onSelect(contact, openConversations)}
                  className={`
                    flex items-start gap-3 px-4 py-4 text-left border-b
                    border-neutro-2
                    hover:bg-accent/20
                    ${activeChatId === contact.id ? 'bg-neutro-3' : ''}
                  `}
                >
                  <AvatarContact name={contact.name} lastName={contact.lastName} />

                  <div className="flex-1">

                    <div className="flex justify-between">
                      <span className="text-sm font-medium">
                        {contact.name} {contact.lastName}
                      </span>
                      <span className='text-xs text-neutro-2'>no leídos</span>
                    </div>

                    <div className="flex justify-between">
                      <span className="text-xs text-gray-400">
                        {getStatusLabel(contact.funnelStatus).toUpperCase()}
                      </span>

                        <span className="bg-neutro-3 border border-white text-primary text-xs rounded-full w-5 h-5 flex items-center justify-center">
                          {contact.totalUnreadCount}
                        </span>
                    
                    </div>

                    {/* CANALES */}
                    <div className="flex justify-between mt-1">
                      <span className="text-xs text-gray-500">
                     
                      </span>

                      <div className="flex gap-1">
                        {channels.includes('WHATSAPP') && (
                          <MessageCircle className="w-4 h-4 text-green-500" />
                        )}
                        {channels.includes('EMAIL') && (
                          <Mail className="w-4 h-4 text-blue-500" />
                        )}
                      </div>
                    </div>

                  </div>
                </button>
              );
            })}
        </div>
      </ScrollArea>
    </div>
  );
};