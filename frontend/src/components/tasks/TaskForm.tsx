// src/components/tasks/TaskForm.tsx
import { useEffect, useState } from "react";
import { Input } from "../ui/Input";
import { Button } from "../ui/Button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../ui/select";
import type { TaskReqType } from "../../types/task.types";
import type { ContactResType } from "../../types/contact.types";
import { dateAdapter } from "../../utils/dateAdapter";


type Props = {
  contactId?: number;
  contacts?: ContactResType[];
  initialData?: TaskReqType;
  onSubmit: (data: TaskReqType) => void;
  onCancel: () => void;
  isAdminView?: boolean; // Solo para cambiar título, no para asignar
  isLoading: boolean;
};

const TASK_TYPES = [
  { value: "CALL", label: "Llamada" },
  { value: "MEETING", label: "Reunión" },
  { value: "EMAIL", label: "Email" },
  { value: "DEMO", label: "Demo" },
  { value: "OTHER", label: "Otro" },
];

export const TaskForm = ({
  contactId,
  onSubmit,
  onCancel,
  initialData,
  contacts,
  isAdminView = false,
  isLoading = false,
}: Props) => {
  const [form, setForm] = useState<TaskReqType>(
    initialData ?? {
      title: "",
      description: "",
      type: "EMAIL",
      dueDate: "",
      contactId: contactId ?? 0,
    }
  );

  const shouldShowContactSelect = !contactId && !initialData && contacts && contacts.length > 0;

  const handleChange = (key: keyof TaskReqType, value: any) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = () => {
    if (!form.contactId || form.contactId === 0) {
      alert("Debes seleccionar un contacto");
      return;
    }
    if (!form.title.trim()) {
      alert("Debes ingresar un título");
      return;
    }

    const payload = {
      ...form,
      dueDate: dateAdapter.toBackend(form.dueDate),
    }
    onSubmit(payload)
  }

  const selectedContact = contacts?.find(
    (c) => c.id === form.contactId
  );

  useEffect(() => {
    if (initialData) {
      setForm({
        ...initialData,
        dueDate: dateAdapter.toInput(initialData.dueDate),
      });
    } else if (contactId) {
      setForm((prev) => ({ ...prev, contactId }));
    }
  }, [initialData, contactId]);

  return (
    <div className="flex flex-col gap-4">
      {/* Selector de contacto - visible cuando no hay contactId fijo */}
      {shouldShowContactSelect && (
        <div className="flex flex-col gap-1">
          <label className="text-sm text-neutro-1 font-medium">Contacto</label>
          <Select
            value={String(form.contactId)}
            onValueChange={(value) => handleChange("contactId", Number(value))}
          >
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Seleccionar contacto">
                {selectedContact
                  ? `${selectedContact.name} ${selectedContact.lastName}`
                  : "Seleccionar contacto"}
              </SelectValue>
            </SelectTrigger>

            <SelectContent className="bg-white">
              {contacts.map((c) => (
                <SelectItem key={c.id} value={String(c.id)}>
                  {c.name} {c.lastName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <p className="text-xs text-gray-500 mt-1">
            La tarea se asignará automáticamente al vendedor dueño de este contacto
          </p>
        </div>
      )}

      {/* Mostrar contacto seleccionado cuando es fijo */}
      {contactId && !initialData && selectedContact && (
        <div className="flex flex-col gap-1">
          <label className="text-sm text-neutro-1 font-medium">Contacto</label>
          <div className="bg-gray-50 p-3 rounded-lg border border-gray-200 text-sm">
            {selectedContact.name} {selectedContact.lastName}
          </div>
        </div>
      )}

      <Input
        label="Título"
        placeholder="Ej: Llamar para seguimiento"
        value={form.title}
        onChange={(e) => handleChange("title", e.target.value)}
      />

      <div className="flex flex-col gap-1">
        <span className="text-sm text-neutro-1 font-medium">Descripción</span>
        <textarea
          placeholder="Detalles de la tarea..."
          value={form.description}
          onChange={(e) => handleChange("description", e.target.value)}
          rows={3}
          className="bg-white border border-neutro-2 text-neutro-1 rounded-xl px-4 py-2.5 text-sm
            focus:outline-none focus:border-secondary placeholder:text-neutro-2 resize-none"
        />
      </div>

      <div className="flex flex-col gap-1">
        <span className="text-sm text-neutro-1 font-medium">Tipo de tarea</span>
        <Select
          value={form.type}
          onValueChange={(value) => handleChange("type", value)}
        >
          <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
            <SelectValue placeholder="Tipo de tarea" />
          </SelectTrigger>

          <SelectContent className="bg-white">
            {TASK_TYPES.map((t) => (
              <SelectItem key={t.value} value={t.value}>
                {t.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <Input
        label="Fecha límite"
        type="datetime-local"
        value={form.dueDate}
        onChange={(e) => handleChange("dueDate", e.target.value)}
      />

      <div className="flex justify-end gap-2 pt-2 w-full">
        <Button variant="outline" onClick={onCancel} className="w-1/2">
          Cancelar
        </Button>
        <Button
          onClick={handleSubmit}
          className="w-1/2"
          disabled={isLoading}
        >
          {isLoading
            ? "Guardando..."
            : initialData
              ? "Guardar cambios"
              : "Crear tarea"}
        </Button>
      </div>
    </div>
  );
};