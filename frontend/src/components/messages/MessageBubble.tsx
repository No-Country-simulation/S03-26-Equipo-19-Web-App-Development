import type { Message } from '../../types/message.types';

interface MessageBubbleProps {
  message: Message;
}

const formatTime = (iso: string) => {
  return new Date(iso).toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' });
};

export const MessageBubble = ({ message }: MessageBubbleProps) => {
  const isOutbound = message.direction === 'outbound';

  return (
    <div className={`flex ${isOutbound ? 'justify-end' : 'justify-start'} mb-3`}>
      <div
        className={`
          max-w-xs lg:max-w-md px-4 py-2.5 rounded-2xl text-sm
          ${isOutbound
            ? 'bg-sky-500 text-white rounded-br-sm'
            : 'bg-gray-800 text-gray-200 rounded-bl-sm'
          }
        `}
      >
        <p>{message.content}</p>
        <span className={`text-xs mt-1 block ${isOutbound ? 'text-sky-100' : 'text-gray-500'}`}>
          {formatTime(message.sentAt)}
        </span>
      </div>
    </div>
  );
};