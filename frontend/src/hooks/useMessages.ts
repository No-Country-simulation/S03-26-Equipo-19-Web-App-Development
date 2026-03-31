import { useState, useEffect } from 'react';
import type { Conversation, Message } from '../types/message.types';
// import { messagesService } from '../services/messages.service';

const MOCK_CONVERSATIONS: Conversation[] = [
  {
    contactId: '1',
    contactName: 'Ana García',
    channel: 'whatsapp',
    lastMessage: 'Perfecto, quedamos para el lunes entonces.',
    lastMessageAt: '2024-03-12T10:30:00',
    unreadCount: 2,
    messages: [
      { id: 'm1', contactId: '1', channel: 'whatsapp', direction: 'inbound', content: 'Hola! Me interesa el producto.', sentAt: '2024-03-12T09:00:00', read: true },
      { id: 'm2', contactId: '1', channel: 'whatsapp', direction: 'outbound', content: 'Hola Ana! Con gusto te cuento más.', sentAt: '2024-03-12T09:15:00', read: true },
      { id: 'm3', contactId: '1', channel: 'whatsapp', direction: 'inbound', content: 'Perfecto, quedamos para el lunes entonces.', sentAt: '2024-03-12T10:30:00', read: false },
    ],
  },
  {
    contactId: '2',
    contactName: 'Carlos Méndez',
    channel: 'email',
    lastMessage: 'Adjunto la propuesta que mencioné.',
    lastMessageAt: '2024-03-11T15:00:00',
    unreadCount: 0,
    messages: [
      { id: 'm4', contactId: '2', channel: 'email', direction: 'outbound', content: 'Hola Carlos, te envío la propuesta.', sentAt: '2024-03-11T14:00:00', read: true },
      { id: 'm5', contactId: '2', channel: 'email', direction: 'inbound', content: 'Adjunto la propuesta que mencioné.', sentAt: '2024-03-11T15:00:00', read: true },
    ],
  },
  {
    contactId: '3',
    contactName: 'Sofía Torres',
    channel: 'whatsapp',
    lastMessage: '¿Podemos agendar una llamada?',
    lastMessageAt: '2024-03-10T08:45:00',
    unreadCount: 1,
    messages: [
      { id: 'm6', contactId: '3', channel: 'whatsapp', direction: 'inbound', content: '¿Podemos agendar una llamada?', sentAt: '2024-03-10T08:45:00', read: false },
    ],
  },
];

export const useMessages = () => {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [activeConversation, setActiveConversation] = useState<Conversation | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        setLoading(true);
        // const data = await messagesService.getConversations();
        await new Promise(res => setTimeout(res, 400));
        setConversations(MOCK_CONVERSATIONS);
        setActiveConversation(MOCK_CONVERSATIONS[0]);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  const sendMessage = (content: string) => {
    if (!activeConversation || !content.trim()) return;

    const newMessage: Message = {
      id: crypto.randomUUID(),
      contactId: activeConversation.contactId,
      channel: activeConversation.channel,
      direction: 'outbound',
      content,
      sentAt: new Date().toISOString(),
      read: true,
    };

    setConversations(prev =>
      prev.map(c =>
        c.contactId === activeConversation.contactId
          ? { ...c, messages: [...c.messages, newMessage], lastMessage: content }
          : c
      )
    );

    setActiveConversation(prev =>
      prev ? { ...prev, messages: [...prev.messages, newMessage] } : prev
    );
  };

  const selectConversation = (contactId: string) => {
    const conv = conversations.find(c => c.contactId === contactId);
    if (conv) setActiveConversation(conv);
  };

  return { conversations, activeConversation, loading, sendMessage, selectConversation };
};