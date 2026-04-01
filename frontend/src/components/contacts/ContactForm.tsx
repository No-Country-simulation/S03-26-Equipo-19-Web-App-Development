import { useState } from 'react';
import type { Contact, FunnelStage, Channel } from '../../types/contact.types';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';

type ContactFormData = Omit<Contact, 'id' | 'createdAt' | 'lastContactedAt'>;

interface ContactFormProps {
  onSubmit: (data: ContactFormData) => void;
  onCancel: () => void;
}

export const ContactForm = ({ onSubmit, onCancel }: ContactFormProps) => {
  const [form, setForm] = useState<ContactFormData>({
    name: '',
    email: '',
    phone: '',
    stage: 'lead',
    channel: 'whatsapp',
    tags: [],
  });

  const handleChange = (field: keyof ContactFormData, value: string) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = () => {
    if (!form.name || !form.email) return;
    onSubmit(form);
  };

  return (
    <div className="flex flex-col gap-4">
      <Input
        label="Nombre"
        placeholder="Ej: Ana García"
        value={form.name}
        onChange={e => handleChange('name', e.target.value)}
      />
      <Input
        label="Email"
        type="email"
        placeholder="ana@email.com"
        value={form.email}
        onChange={e => handleChange('email', e.target.value)}
      />
      <Input
        label="Teléfono"
        placeholder="+54 351 123 4567"
        value={form.phone}
        onChange={e => handleChange('phone', e.target.value)}
      />

      <div className="flex flex-col gap-1">
        <label className="text-sm text-gray-400 font-medium">Estado</label>
        <select
          value={form.stage}
          onChange={e => handleChange('stage', e.target.value as FunnelStage)}
          className="bg-gray-800 border border-gray-700 text-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-sky-500"
        >
          <option value="lead">Lead</option>
          <option value="active">Activo</option>
          <option value="following">En seguimiento</option>
          <option value="closed">Cerrado</option>
        </select>
      </div>

      <div className="flex flex-col gap-1">
        <label className="text-sm text-gray-400 font-medium">Canal preferido</label>
        <select
          value={form.channel}
          onChange={e => handleChange('channel', e.target.value as Channel)}
          className="bg-gray-800 border border-gray-700 text-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-sky-500"
        >
          <option value="whatsapp">WhatsApp</option>
          <option value="email">Email</option>
        </select>
      </div>

      <div className="flex gap-3 justify-end mt-2">
        <Button variant="secondary" onClick={onCancel}>Cancelar</Button>
        <Button onClick={handleSubmit}>Guardar contacto</Button>
      </div>
    </div>
  );
};