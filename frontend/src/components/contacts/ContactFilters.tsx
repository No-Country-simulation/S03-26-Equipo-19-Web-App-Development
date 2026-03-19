import type { FunnelStage, Channel } from '../../types/contact.types';
import { Input } from '../ui/Input';

interface ContactFiltersProps {
  search: string;
  stage: FunnelStage | 'all';
  channel: Channel | 'all';
  onSearchChange: (value: string) => void;
  onStageChange: (value: FunnelStage | 'all') => void;
  onChannelChange: (value: Channel | 'all') => void;
}

export const ContactFilters = ({
  search,
  stage,
  channel,
  onSearchChange,
  onStageChange,
  onChannelChange,
}: ContactFiltersProps) => {
  return (
    <div className="flex flex-col sm:flex-row gap-3 mb-6">
      <Input
        placeholder="Buscar contacto..."
        value={search}
        onChange={e => onSearchChange(e.target.value)}
        className="sm:w-64"
      />

      <select
        value={stage}
        onChange={e => onStageChange(e.target.value as FunnelStage | 'all')}
        className="bg-gray-800 border border-gray-700 text-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-sky-500"
      >
        <option value="all">Todos los estados</option>
        <option value="lead">Lead</option>
        <option value="active">Activo</option>
        <option value="following">En seguimiento</option>
        <option value="closed">Cerrado</option>
      </select>

      <select
        value={channel}
        onChange={e => onChannelChange(e.target.value as Channel | 'all')}
        className="bg-gray-800 border border-gray-700 text-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-sky-500"
      >
        <option value="all">Todos los canales</option>
        <option value="whatsapp">WhatsApp</option>
        <option value="email">Email</option>
      </select>
    </div>
  );
};