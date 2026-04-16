import { useState } from 'react';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../ui/select'; // Asegúrate de que la ruta sea correcta (Select.tsx o select.tsx)
import { useTasksMutationsService } from '../../services/use_mutations/tasks-mutation';
import { useGetSalespersons } from '../../services/use_queries/salespersons-query';
import { useGetContacts } from '../../services/use_queries/contacts-query';
import type { TaskReqType } from '../../types/task.types';

// Tipo interno del formulario (lo que el usuario ve)
type FormTaskType = 'follow-up' | 'reminder';

type TaskAdminFormData = {
  title: string;
  description: string;
  type: FormTaskType;
  dueDate: string;
  assignedTo: string; // Cambiado de userId a assignedTo
  contactId: string;
};

type Props = {
  onCancel: () => void;
  onSuccess?: () => void;
};

// Mapeo del tipo interno al tipo de la API
const mapFormTypeToApiType = (formType: FormTaskType): TaskReqType['type'] => {
  switch (formType) {
    case 'follow-up':
      return 'CALL'; // o 'EMAIL', según tu lógica de negocio
    case 'reminder':
      return 'OTHER';
    default:
      return 'OTHER';
  }
};

export const TaskAdminForm = ({ onCancel, onSuccess }: Props) => {
  const [form, setForm] = useState<TaskAdminFormData>({
    title: '',
    description: '',
    type: 'follow-up',
    dueDate: '',
    assignedTo: '',
    contactId: '',
  });

  const { mutationPostTask } = useTasksMutationsService();
  const { data: salespersons = [] } = useGetSalespersons();
  const { data: contacts = [] } = useGetContacts();

  const handleChange = (key: keyof TaskAdminFormData, value: string) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = () => {
    if (!form.title.trim()) return;

    const apiData: TaskReqType = {
      title: form.title,
      description: form.description,
      type: mapFormTypeToApiType(form.type),
      dueDate: form.dueDate, // string en formato YYYY-MM-DD
      contactId: Number(form.contactId),
      // Asegúrate de que TaskReqType incluya assignedTo (ver sugerencia abajo)
      assignedTo: form.assignedTo ? Number(form.assignedTo) : undefined,
    };

    mutationPostTask.mutate(apiData, {
      onSuccess: () => onSuccess?.(),
    });
  };

  return (
    <div className="flex flex-col gap-4">
      <Input
        label="Nombre de la tarea"
        placeholder="Llamar a Juan por propuesta"
        value={form.title}
        onChange={(e) => handleChange('title', e.target.value)}
      />

      <div className="flex flex-col gap-1">
        <span className="text-sm text-neutro-1 font-medium">Descripción</span>
        <textarea
          placeholder="Confirmar interés y enviar cotización"
          value={form.description}
          onChange={(e) => handleChange('description', e.target.value)}
          rows={3}
          className="bg-white border border-neutro-2 text-neutro-1 rounded-xl px-4 py-2.5 text-sm
            focus:outline-none focus:border-secondary placeholder:text-neutro-2 resize-none"
        />
      </div>

      <div className="flex flex-col gap-2">
        <span className="text-sm text-neutro-1 font-medium">Tipo de tarea</span>
        <div className="flex gap-4">
          {(['follow-up', 'reminder'] as FormTaskType[]).map((t) => (
            <label key={t} className="flex items-center gap-2 cursor-pointer">
              <div
                onClick={() => handleChange('type', t)}
                className={`w-4 h-4 rounded-full border-2 flex items-center justify-center transition-colors ${
                  form.type === t ? 'border-blue-500 bg-blue-500' : 'border-slate-300'
                }`}
              >
                {form.type === t && <div className="w-1.5 h-1.5 rounded-full bg-white" />}
              </div>
              <span className="text-sm text-neutro-1">
                {t === 'follow-up' ? 'Seguimiento' : 'Recordatorio'}
              </span>
            </label>
          ))}
        </div>
      </div>

      <Input
        label="Fecha límite"
        type="date"
        value={form.dueDate}
        onChange={(e) => handleChange('dueDate', e.target.value)}
      />

      <div className="flex flex-col gap-1">
        <span className="text-sm text-neutro-1 font-medium">Asignado a</span>
        <Select value={form.assignedTo} onValueChange={(val) => handleChange('assignedTo', val ?? '')}>
          <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
            <SelectValue placeholder="Seleccionar vendedor" />
          </SelectTrigger>
          <SelectContent className="bg-white">
            {salespersons.map((s) => (
              <SelectItem key={s.id} value={String(s.id)}>
                {s.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="flex flex-col gap-1">
        <span className="text-sm text-neutro-1 font-medium">Contacto relacionado</span>
        <Select value={form.contactId} onValueChange={(val) => handleChange('contactId', val ?? '')}>
          <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
            <SelectValue placeholder="Seleccionar contacto" />
          </SelectTrigger>
          <SelectContent className="bg-white">
            {contacts.map((c) => (
              <SelectItem key={c.id} value={String(c.id)}>
                {c.name} {c.lastName}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="flex justify-end gap-2 pt-2 w-full">
        <Button variant="outline" onClick={onCancel} className="w-1/2">
          Cancelar
        </Button>
        <Button
          onClick={handleSubmit}
          loading={mutationPostTask.isPending} // Corrección aquí
          className="w-1/2"
        >
          Guardar
        </Button>
      </div>
    </div>
  );
};