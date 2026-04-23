import { useEffect, useState } from 'react';
import type { Channel, ContactReqType } from '../../types/contact.types';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';

interface ContactFormProps {
  onSubmit: (data: ContactReqType) => void;
  onCancel?: () => void;
  initialData?: ContactReqType; //Enviar para update
  isLoading?: boolean;
}

export const ContactForm = ({
  onSubmit,
  onCancel,
  initialData,
  isLoading = false,
}: ContactFormProps) => {

  const [form, setForm] = useState<ContactReqType>({
    contact: {
      name: '',
      lastName: '',
      email: '',
      phone: '',
      company: '',
    },
    preferredChannel: 'WHATSAPP',
  });


  useEffect(() => {
    if (initialData) {
      setForm(initialData);
    }
  }, [initialData]);

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

  const buildPayload = (): ContactReqType => {
    return {
      contact: {
        name: form.contact.name,
        lastName: form.contact.lastName,
        email: form.contact.email,
        phone: form.contact.phone,
        ...(form.contact.company && { company: form.contact.company }),
      },
      preferredChannel: form.preferredChannel.toUpperCase() as Channel,
    };
  };

  const handleSubmit = () => {
    if (!form.contact.name || !form.contact.email) return;

    const cleanData = buildPayload();
    onSubmit(cleanData);
  };

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
            onChange={e =>
              handleRootChange(
                'preferredChannel',
                e.target.value as Channel
              )
            }
            className="bg-white border border-neutro-2 text-neutro-1
              rounded-xl px-4 py-2.5 text-sm
              focus:outline-none focus:border-secondary"
          >
            <option value="WHATSAPP">WhatsApp</option>
            <option value="EMAIL">Email</option>
          </select>
        </div>

      </div>

      <div className="flex justify-center gap-2">

        {onCancel && (
          <Button
            variant="outline"
            className="mt-8 w-full"
            onClick={onCancel}
            disabled={isLoading}
          >
            Cancelar
          </Button>
        )}

        <Button
          className="mt-8 w-full"
          onClick={handleSubmit}
          disabled={isLoading}
        >
          {isLoading
            ? initialData
              ? "Guardando..."
              : "Creando..."
            : initialData
              ? "GUARDAR CAMBIOS"
              : "CREAR CONTACTO"}
        </Button>

      </div>
    </>
  );
};