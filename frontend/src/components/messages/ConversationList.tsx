import type { ConversationResType } from '../../types/conversation.types';
import { formatTime } from '../../utils/formateDate';
import AvatarContact from '../ui/AvatarContact';
import TitleSection from '../ui/TitleSection';
import { useState } from 'react';
import { Tabs, TabsList, TabsTrigger } from '../ui/tabs';
import { Badge } from '../ui/Badge';
import { ScrollArea } from '../ui/scroll-area';



interface ConversationListProps {
  conversations: ConversationResType[];
  activeChatId: number | null;
  onSelect: (id: number) => void;
}


export const ConversationList = ({ conversations, activeChatId, onSelect }: ConversationListProps) => {

  const [activeStatus, setActiveStatus] = useState()
  const [totalMessages, setTotalMessages] = useState(1)
  const [noReadMessages, setNoReadMessages] = useState(1)

  return (
    <div className="relative">
      <div className='bg-white rounded-lg shadow-lg pt-8 pb-6 px-6 z-10 relative'>
        <TitleSection text="Mis Conversaciones" className='hidden md:flex' />
        <Tabs value={activeStatus} onValueChange={() => setActiveStatus} >
          <TabsList className="bg-neutro-2/30 rounded-2xl lg:mt-4">
            <TabsTrigger value="WHATSAPP">
              Total
              {totalMessages > 0 && (
                <Badge className="border border-primary/30 bg-white text-primary">{totalMessages}</Badge>
              )}
            </TabsTrigger>

            <TabsTrigger value="EMAIL">
              No leídos
              {noReadMessages > 0 && (
                <Badge className="border border-primary/30 bg-white text-primary">
                  {noReadMessages}
                </Badge>
              )}
            </TabsTrigger>
          </TabsList>
        </Tabs>
      </div>
      <ScrollArea >
        <div className="flex flex-col h-full overflow-y-auto bg-white -mt-4 pt-5">
          {conversations?.map(conv => (
            <button
              key={conv.contact.id}
              onClick={() => onSelect(conv.contact.id)}
              className={`
            flex items-start gap-3 px-4 py-4 text-left border-b border-gray-800
            hover:bg-accent/20 transition-colors
            ${activeChatId === conv.contact.id ? 'bg-neutro-3' : ''}
          `}
            >
              <AvatarContact name={conv.contact.name} lastName={conv.contact.lastName} />

              <div className="flex-1 min-w-0">
                <div className="flex justify-between items-center mb-0.5">
                  <span className="text-primary text-sm font-medium truncate">{conv.contact.name}</span>
                  <span className="text-gray-500 text-xs shrink-0 ml-2">{formatTime(conv.lastInteraction)}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-400 text-xs truncate">{conv.status}</span>
                  {conv.status !== "READ" && (
                    <span className="bg-sky-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center shrink-0 ml-2">
                      {conv.id}
                    </span>
                  )}
                </div>
                <span className="text-xs text-gray-600 capitalize mt-0.5 block">{conv.channel}</span>
              </div>
            </button>
          ))}
        </div>
      </ScrollArea>
    </div>
  );
};