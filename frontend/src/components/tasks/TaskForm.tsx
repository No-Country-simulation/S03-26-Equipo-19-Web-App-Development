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
};

const TASK_TYPES = [
  { value: "CALL", label: "Llamada" },
  { value: "MEETING", label: "Reunión" },
  { value: "EMAIL", label: "Email" },
  { value: "DEMO", label: "Demo" },
  { value: "OTHER", label: "Otro" },
];

export const TaskForm = ({ contactId, onSubmit, onCancel, initialData, contacts }: Props) => {

  const [form, setForm] = useState<TaskReqType>(
    initialData ?? {
      title: "",
      description: "",
      type: "EMAIL",
      dueDate: "",
      contactId: contactId ?? 1,
    }
  );

  const shouldShowContactSelect = !contactId && !initialData;

  const handleChange = (key: keyof TaskReqType, value: any) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = () => {
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
      {shouldShowContactSelect && contacts && (
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

          <SelectContent className={"bg-white"}>
            {contacts.map((c) => (
              <SelectItem key={c.id} value={String(c.id)}>
                {c.name} {c.lastName}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      )}
      <Input
        placeholder="Título"
        value={form.title}
        onChange={(e) => handleChange("title", e.target.value)}
      />


      <Input
        placeholder="Descripción"
        value={form.description}
        onChange={(e) => handleChange("description", e.target.value)}
      />

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

      <Input
        type="datetime-local"
        value={form.dueDate}
        onChange={(e) => handleChange("dueDate", e.target.value)}
      />

      <div className="flex justify-end gap-2 pt-2 w-full">
        <Button variant="outline" onClick={onCancel} className="w-1/2">
          Cancelar
        </Button>
        <Button onClick={handleSubmit} className="w-1/2">
          {initialData ? "Guardar cambios" : "Crear tarea"}
        </Button>
      </div>
    </div>
  );
};