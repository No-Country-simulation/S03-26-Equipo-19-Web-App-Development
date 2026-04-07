import { useState } from 'react';
import type { Channel, ContactReqType } from '../../types/contact.types';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { useGetContactById, useGetContacts } from '../../services/use_queries/contacts-query';


interface ContactFormProps {
  id: number; 
  onSubmit: (data: ContactReqType) => void;
  onCancel: () => void;
}

export const ContactEditForm = ({ id, onSubmit, onCancel }: ContactFormProps) => {


 const { data: contactData, isLoading } = useGetContactById(id);
  


  const [form, setForm] = useState<ContactReqType>({
    contact: {
      name: contactData?.name || '',
      lastName: contactData?.lastName || '',
      email: contactData?.email || '',
      phone: contactData?.phone || '',
      company: contactData?.company || '',
    },
    funnelStatus: 'NEW_LEAD',
    preferredChannel: contactData?.preferredChannel || 'whatsapp',
    ownerId: 0,
    tags: [],
  });

  const handleContactChange = (
    field: keyof ContactReqType['contact'],
    value: string
  ) => {
    setForm(prev => ({
      ...prev,
      contact: {
        ...prev.contact,
        [field]: value,
      },
    }));
  };

  const handleRootChange = (
    field: keyof ContactReqType,
    value: any
  ) => {
    setForm(prev => ({
      ...prev,
      [field]: value,
    }));
  };


  const handleSubmit = () => {
    if (!form.contact.name || !form.contact.email) return;
    onSubmit(form);
  };

if (isLoading) return <div className="flex items-center justify-center">
    <p className="text-lg font-medium text-primary">Cargando datos de contacto...</p>
  </div>;

  return (
    <>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Input
          label="Nombre"
          placeholder="Ej: Ana"
          value={form.contact.name}
          onChange={e => handleContactChange('name', e.target.value)}
        />
        <Input
          label="Apellido"
          placeholder="Ej: García"
          value={form.contact.lastName}
          onChange={e => handleContactChange('lastName', e.target.value)}
        />
        <Input
          label="Email"
          type="email"
          placeholder="ana@email.com"
          value={form.contact.email}
          onChange={e => handleContactChange('email', e.target.value)}
        />
        <Input
          label="Teléfono"
          placeholder="+54 351 123 4567"
          value={form.contact.phone}
          onChange={e => handleContactChange('phone', e.target.value)}
        />

        <Input
          label="Empresa"
          placeholder="Ej: Company S.A."
          value={form.contact.company}
          onChange={e => handleContactChange('company', e.target.value)}
        />

        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium">Canal preferido</label>
          <select
            value={form.preferredChannel}
            onChange={e => handleRootChange('preferredChannel', e.target.value as Channel)}
            className=" bg-white border border-neutro-2 text-neutro-1
          rounded-xl px-4 py-2.5 text-sm
          focus:outline-none focus:border-secondary
          placeholder:text-neutro-2"
          >
            <option value="whatsapp">WhatsApp</option>
            <option value="email">Email</option>
          </select>
        </div>
      </div>
      <div className="flex justify-center">
        <Button className="mt-8 w-full" onClick={handleSubmit}>
          CREAR CONTACTO
        </Button>
      </div>
    </>
  );
};