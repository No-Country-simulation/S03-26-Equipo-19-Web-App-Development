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
import { SalespersonsMutationsService } from '../../services/use_mutations/salespersons-mutation';

type UserRole = 'SALESPERSON' | 'ADMIN';

type UserManagementFormData = {
  name: string;
  email: string;
  role: UserRole;
  password: string;
};

type Props = {
  onCancel: () => void;
  onSuccess?: () => void;
};

export const UserManagementForm = ({ onCancel, onSuccess }: Props) => {
  const [form, setForm] = useState<UserManagementFormData>({
    name: '',
    email: '',
    role: 'SALESPERSON',
    password: '',
  });

  const { mutationCreateSalesperson } = SalespersonsMutationsService();

  const handleChange = (key: keyof UserManagementFormData, value: string) => {
    setForm(prev => ({ ...prev, [key]: value }));
  };

  const handleSubmit = () => {
    if (!form.name || !form.email || !form.password) return;
    mutationCreateSalesperson.mutate(
      { name: form.name, email: form.email, password: form.password },
      { onSuccess: () => onSuccess?.() },
    );
  };

  return (
    <div className="flex flex-col gap-4">
      <Input
        label="Nombre"
        placeholder="Ej.: Juan Pérez"
        value={form.name}
        onChange={e => handleChange('name', e.target.value)}
      />

      <Input
        label="Correo"
        type="email"
        placeholder="juanperez@conversa.com"
        value={form.email}
        onChange={e => handleChange('email', e.target.value)}
      />

      <div className="grid grid-cols-2 gap-3">
        <div className="flex flex-col gap-1">
          <span className="text-sm text-neutro-1 font-medium">Rol</span>
          <Select
            value={form.role}
            onValueChange={val => handleChange('role', val as UserRole)}
          >
            <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
              <SelectValue placeholder="Rol" />
            </SelectTrigger>
            <SelectContent className="bg-white">
              <SelectItem value="SALESPERSON">Vendedor</SelectItem>
              <SelectItem value="ADMIN">Admin</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <Input
          label="Contraseña"
          type="password"
          placeholder="••••••••"
          value={form.password}
          onChange={e => handleChange('password', e.target.value)}
        />
      </div>

      <p className="text-xs text-slate-400 flex items-start gap-2">
        <span className="text-blue-400 mt-0.5">ℹ</span>
        Al guardar, el usuario recibirá un correo para confirmar su cuenta y acceder al sistema
      </p>

      <div className="flex justify-end gap-2 pt-2 w-full">
        <Button variant="outline" onClick={onCancel} className="w-1/2">
          Cancelar
        </Button>
        <Button
          onClick={handleSubmit}
          loading={mutationCreateSalesperson.isPending}
          className="w-1/2"
        >
          Guardar
        </Button>
      </div>
    </div>
  );
};