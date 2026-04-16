import { useState, useRef } from 'react';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../ui/select';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiTemplatesService } from '../../services/general_api';

type TemplateChannel = 'EMAIL' | 'WHATSAPP';

type EmailTemplateFormData = {
  name: string;
  channel: TemplateChannel;
  subject: string;
  body: string;
};

type Props = {
  onCancel: () => void;
  onSuccess?: () => void;
};

const DYNAMIC_FIELDS = [
  '{{name}}',
  '{{email}}',
  '{{phone}}',
  '{{company}}',
  '{{today_date}}',
  '{{user_name}}',
];

// Extraer todas las ocurrencias de {{...}} en un texto
const extractVariables = (text: string): string[] => {
  const regex = /\{\{([^}]+)\}\}/g;
  const matches = text.match(regex) || [];
  return [...new Set(matches)]; // eliminar duplicados
};

// Convertir array de placeholders en objeto { variable: "valor por defecto" }
const buildVariablesObject = (vars: string[]): Record<string, string> => {
  const obj: Record<string, string> = {};
  vars.forEach((v) => {
    const key = v.replace(/[{}]/g, ''); // quita {{ y }}
    obj[key] = key; // el valor por defecto puede ser el nombre de la variable
  });
  return obj;
};

export const EmailTemplateForm = ({ onCancel, onSuccess }: Props) => {
  const [form, setForm] = useState<EmailTemplateFormData>({
    name: '',
    channel: 'EMAIL',
    subject: '',
    body: '',
  });
  const bodyRef = useRef<HTMLTextAreaElement>(null);
  const qc = useQueryClient();

  const mutation = useMutation({
    mutationFn: (data: any) => apiTemplatesService.post('', data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['templates'] });
      onSuccess?.();
    },
  });

  const handleChange = (key: keyof EmailTemplateFormData, value: string) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const insertField = (field: string) => {
    const el = bodyRef.current;
    if (!el) return;
    const start = el.selectionStart ?? form.body.length;
    const end = el.selectionEnd ?? start;
    const newBody = form.body.slice(0, start) + field + form.body.slice(end);
    handleChange('body', newBody);
    setTimeout(() => {
      el.focus();
      el.setSelectionRange(start + field.length, start + field.length);
    }, 0);
  };

  const handleSubmit = () => {
    if (!form.name.trim() || !form.body.trim()) return;

    // Extraer variables del cuerpo y del asunto (si es email)
    const bodyVars = extractVariables(form.body);
    const subjectVars = form.channel === 'EMAIL' ? extractVariables(form.subject) : [];
    const allVariables = [...new Set([...bodyVars, ...subjectVars])];

    // Construir el objeto exacto que espera el backend
    const payload: any = {
      name: form.name.trim(),
      channel: form.channel,
      body: form.body.trim(),
      variables: buildVariablesObject(allVariables),
    };

    // Agregar subject solo si el canal es EMAIL
    if (form.channel === 'EMAIL') {
      payload.subject = form.subject.trim();
    }

    mutation.mutate(payload);
  };

  return (
    <div className="flex flex-col gap-4">
      {/* El resto del JSX permanece exactamente igual */}
      <div className="grid grid-cols-3 gap-3">
        <div className="col-span-2">
          <Input
            label="Nombre de la plantilla"
            placeholder="Seguimiento de clientes potenciales"
            value={form.name}
            onChange={(e) => handleChange('name', e.target.value)}
          />
        </div>
        <div className="flex flex-col gap-1">
          <span className="text-sm text-neutro-1 font-medium">Canal</span>
          <Select
            value={form.channel}
            onValueChange={(val) => handleChange('channel', val as TemplateChannel)}
          >
            <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
              <SelectValue />
            </SelectTrigger>
            <SelectContent className="bg-white">
              <SelectItem value="EMAIL">Email</SelectItem>
              <SelectItem value="WHATSAPP">WhatsApp</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {form.channel === 'EMAIL' && (
        <Input
          label="Asunto del correo"
          placeholder="Hola {{name}}, te contactamos desde {{company}}"
          value={form.subject}
          onChange={(e) => handleChange('subject', e.target.value)}
        />
      )}

      <div className="grid grid-cols-3 gap-3">
        <div className="col-span-2 flex flex-col gap-1">
          <span className="text-sm text-neutro-1 font-medium">Cuerpo</span>
          <textarea
            ref={bodyRef}
            rows={8}
            placeholder={`Hola {{name}},\n\nGracias por tu interés en {{company}}.\n\nSaludos,\n{{user_name}}`}
            value={form.body}
            onChange={(e) => handleChange('body', e.target.value)}
            className="bg-white border border-neutro-2 text-neutro-1 rounded-xl px-4 py-2.5 text-sm
              focus:outline-none focus:border-secondary placeholder:text-neutro-2 resize-none"
          />
        </div>

        <div className="flex flex-col gap-1">
          <span className="text-sm text-neutro-1 font-medium">Campos dinámicos</span>
          <div className="bg-slate-50 rounded-xl p-3 flex flex-col gap-2">
            {DYNAMIC_FIELDS.map((field) => (
              <button
                key={field}
                type="button"
                onClick={() => insertField(field)}
                className="px-3 py-1.5 bg-white border border-neutro-2 rounded-lg text-xs text-neutro-1
                  font-mono hover:bg-blue-50 hover:border-blue-300 hover:text-blue-700 transition-colors text-left"
              >
                {field}
              </button>
            ))}
          </div>
          <p className="text-xs text-slate-400">Clic para insertar en el cursor</p>
        </div>
      </div>

      <div className="flex justify-end gap-2 pt-2 w-full">
        <Button variant="outline" onClick={onCancel} className="w-1/2">
          Cancelar
        </Button>
        <Button
          onClick={handleSubmit}
          loading={mutation.isPending}
          className="w-1/2"
        >
          Guardar
        </Button>
      </div>
    </div>
  );
};