import { useState } from "react";
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



type Props = {
  contactId?: number;
  onSubmit: (data: any) => void;
  onCancel: () => void;
};

const TASK_TYPES = [
  { value: "CALL", label: "Llamada" },
  { value: "MEETING", label: "Reunión" },
  { value: "EMAIL", label: "Email" },
  { value: "DEMO", label: "Demo" },
  { value: "OTHER", label: "Otro" },
];

export const TaskForm = ({ contactId, onSubmit, onCancel }: Props) => {

  const [form, setForm] = useState<TaskReqType>({
    title: "",
    description: "",
    type: "EMAIL",
    dueDate: "",
  });

  const handleChange = (key: keyof TaskReqType, value: any) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

const formatToLocalDateTime = (value: string) => {
  if (!value) return value

  // si viene sin segundos, agregar
  if (value.length === 16) {
    return value + ":00"
  }

  return value
}

  const handleSubmit = () => {
    const payload = {
      ...form,
      dueDate: formatToLocalDateTime(form.dueDate),
      contactId,
    }

    onSubmit(payload)
  }

  return (
    <div className="flex flex-col gap-4">
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
          Crear tarea
        </Button>
      </div>
    </div>
  );
};