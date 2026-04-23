import { useState } from 'react';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';

export type AdminPanelFormData = {
  name: string;
  description: string;
};

type Props = {
  initialValues?: AdminPanelFormData;
  onSubmit: (data: AdminPanelFormData) => void;
  onCancel: () => void;
  isPending?: boolean;
};

export const AdminPanelForm = ({
  initialValues,
  onSubmit,
  onCancel,
  isPending = false,
}: Props) => {
  const [form, setForm] = useState<AdminPanelFormData>({
    name: initialValues?.name || '',
    description: initialValues?.description || '',
  });

  const handleChange = (key: keyof AdminPanelFormData, value: string) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = () => {
    if (!form.name.trim()) {
      alert('El nombre es obligatorio');
      return;
    }
    onSubmit({ name: form.name.trim(), description: form.description.trim() });
  };

  return (
    <div className="flex flex-col gap-4">
      <Input
        label="Nombre de la campaña"
        placeholder="Ej: Lanzamiento Q2"
        value={form.name}
        onChange={(e) => handleChange('name', e.target.value)}
      />
      <div className="flex flex-col gap-1">
        <span className="text-sm text-neutro-1 font-medium">Descripción</span>
        <textarea
          placeholder="Detalles de la campaña..."
          value={form.description}
          onChange={(e) => handleChange('description', e.target.value)}
          rows={3}
          className="bg-white border border-neutro-2 text-neutro-1 rounded-xl px-4 py-2.5 text-sm
            focus:outline-none focus:border-secondary placeholder:text-neutro-2 resize-none"
        />
      </div>

      <div className="flex justify-end gap-2 pt-2 w-full">
        <Button variant="outline" onClick={onCancel} className="w-1/2">
          Cancelar
        </Button>
        <Button onClick={handleSubmit} loading={isPending} className="w-1/2">
          Crear
        </Button>
      </div>
    </div>
  );
};