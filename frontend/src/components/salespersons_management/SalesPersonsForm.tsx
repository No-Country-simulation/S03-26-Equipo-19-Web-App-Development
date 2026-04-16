import { useState } from 'react';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../ui/select';
import type { SalespersonResponse } from '../../types/admin.types';

export type SalespersonFormData = {
  name: string;
  email: string;
  password?: string;
  status: 'ACTIVE' | 'INACTIVE';
};

type Props = {
  initialData?: SalespersonResponse | null;
  onCancel: () => void;
  onSubmit: (data: SalespersonFormData) => void;
  isPending?: boolean;
};

export const SalespersonsForm = ({
  initialData,
  onCancel,
  onSubmit,
  isPending,
}: Props) => {
  const isEditing = !!initialData;

  // ✅ Inicializar el estado directamente con initialData
  const [form, setForm] = useState<SalespersonFormData>(() => ({
    name: initialData?.name || '',
    email: initialData?.email || '',
    password: '',
    status: initialData?.status || 'ACTIVE',
  }));

  const handleChange = (key: keyof SalespersonFormData, value: string) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

    const handleSubmit = () => {
    // Validaciones
    if (!form.name.trim()) {
        alert('El nombre es obligatorio');
        return;
    }
    if (!form.email.trim()) {
        alert('El email es obligatorio');
        return;
    }
    // Validación básica de email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(form.email)) {
        alert('Ingresa un email válido');
        return;
    }
    if (!isEditing) {
        if (!form.password) {
        alert('La contraseña es obligatoria');
        return;
        }
        if (form.password.length < 8) {
        alert('La contraseña debe tener al menos 8 caracteres');
        return;
        }
    }

    const submitData: SalespersonFormData = {
        name: form.name.trim(),
        email: form.email.trim().toLowerCase(),
        status: form.status,
    };

    if (!isEditing) {
        submitData.password = form.password;
    }

    onSubmit(submitData);
    };

  return (
    <div className="flex flex-col gap-4">
      <Input
        label="Nombre completo"
        placeholder="Juan Pérez"
        value={form.name}
        onChange={(e) => handleChange('name', e.target.value)}
      />

      <Input
        label="Correo electrónico"
        type="email"
        placeholder="juan.perez@empresa.com"
        value={form.email}
        onChange={(e) => handleChange('email', e.target.value)}
      />

      {!isEditing && (
        <Input
          label="Contraseña"
          type="password"
          placeholder="••••••••"
          value={form.password || ''}
          onChange={(e) => handleChange('password', e.target.value)}
        />
      )}

      <div className="flex flex-col gap-1">
        <span className="text-sm text-neutro-1 font-medium">Estado</span>
        <Select
          value={form.status}
          onValueChange={(val) => handleChange('status', val as 'ACTIVE' | 'INACTIVE')}
        >
          <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
            <SelectValue />
          </SelectTrigger>
          <SelectContent className="bg-white">
            <SelectItem value="ACTIVE">Activo</SelectItem>
            <SelectItem value="INACTIVE">Inactivo</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="flex justify-end gap-2 pt-2 w-full">
        <Button variant="outline" onClick={onCancel} className="w-1/2">
          Cancelar
        </Button>
        <Button onClick={handleSubmit} loading={isPending} className="w-1/2">
          {isEditing ? 'Actualizar' : 'Crear'}
        </Button>
      </div>
    </div>
  );
};