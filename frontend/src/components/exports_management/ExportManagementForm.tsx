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
import type { ExportFormat, ExportEntity } from '../../types/admin.types';

type Props = {
  onCancel: () => void;
  onSuccess: (format: ExportFormat, entity: ExportEntity) => void;
  isPending?: boolean;
};

const ENTITY_OPTIONS: { value: ExportEntity; label: string }[] = [
  { value: 'CONTACTS', label: 'Contactos' },
  { value: 'USERS', label: 'Usuarios' },
  { value: 'TASKS', label: 'Tareas' },
  { value: 'SALESPERSONS', label: 'Vendedores' },
  { value: 'CONVERSATIONS', label: 'Conversaciones' },
];

export const ExportManagementForm = ({ onCancel, onSuccess, isPending }: Props) => {
  const [format, setFormat] = useState<ExportFormat>('CSV');
  const [entity, setEntity] = useState<ExportEntity>('CONTACTS');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');

  const handleSubmit = () => {
    // Si el backend acepta filtros de fecha, deberías incluirlos en el payload
    onSuccess(format, entity);
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <span className="text-sm text-neutro-1 font-medium">Formato</span>
        <Select value={format} onValueChange={(val) => setFormat(val as ExportFormat)}>
          <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
            <SelectValue />
          </SelectTrigger>
          <SelectContent className="bg-white">
            <SelectItem value="CSV">CSV</SelectItem>
            <SelectItem value="PDF">PDF</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="flex flex-col gap-1">
        <span className="text-sm text-neutro-1 font-medium">Entidad a exportar</span>
        <Select value={entity} onValueChange={(val) => setEntity(val as ExportEntity)}>
          <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
            <SelectValue />
          </SelectTrigger>
          <SelectContent className="bg-white">
            {ENTITY_OPTIONS.map((opt) => (
              <SelectItem key={opt.value} value={opt.value}>
                {opt.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <Input
          label="Fecha desde"
          type="date"
          value={dateFrom}
          onChange={(e) => setDateFrom(e.target.value)}
        />
        <Input
          label="Fecha hasta"
          type="date"
          value={dateTo}
          onChange={(e) => setDateTo(e.target.value)}
        />
      </div>

      <div className="flex justify-end gap-2 pt-2 w-full">
        <Button variant="outline" onClick={onCancel} className="w-1/2">
          Cancelar
        </Button>
        <Button onClick={handleSubmit} loading={isPending} className="w-1/2">
          Exportar
        </Button>
      </div>
    </div>
  );
};