import { useState, useRef, useEffect } from 'react';
import type { Conversation } from '../../types/message.types';
import { MessageBubble } from './MessageBubble';
import { Button } from '../ui/Button';

interface ChatWindowProps {
  conversation: Conversation;
  onSend: (content: string) => void;
}

export const ChatWindow = ({ conversation, onSend }: ChatWindowProps) => {
  const [input, setInput] = useState('');
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [conversation.messages]);

  const handleSend = () => {
    if (!input.trim()) return;
    onSend(input.trim());
    setInput('');
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-700 flex items-center gap-3">
        <div className="w-9 h-9 rounded-full bg-sky-500/20 text-sky-400 flex items-center justify-center font-semibold text-sm">
          {conversation.contactName.charAt(0)}
        </div>
        <div>
          <p className="text-white font-medium text-sm">{conversation.contactName}</p>
          <p className="text-gray-500 text-xs capitalize">{conversation.channel}</p>
        </div>
      </div>

      {/* Mensajes */}
      <div className="flex-1 overflow-y-auto px-6 py-4">
        {conversation.messages.map(msg => (
          <MessageBubble key={msg.id} message={msg} />
        ))}
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <div className="px-6 py-4 border-t border-gray-700 flex gap-3">
        <textarea
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Escribí un mensaje... (Enter para enviar)"
          rows={1}
          className="flex-1 bg-gray-800 border border-gray-700 text-gray-200 rounded-xl px-4 py-2.5 text-sm resize-none focus:outline-none focus:border-sky-500 placeholder:text-gray-500"
        />
        <Button onClick={handleSend}>Enviar</Button>
      </div>
    </div>
  );
};