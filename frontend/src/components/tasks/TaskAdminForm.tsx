import { useState } from "react";
import { Button } from "../ui/Button";
import { Input } from "../ui/Input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../ui/select";
import type { TaskReqType } from "../../types/task.types";

type Props = {
  initialData?: Partial<TaskReqType>;
  contacts: { id: number; name: string }[];
  onSubmit: (data: TaskReqType) => void;
  onCancel: () => void;
};

export const TaskAdminForm = ({
  initialData,
  contacts,
  onSubmit,
  onCancel,
}: Props) => {
  const [form, setForm] = useState<TaskReqType>({
    title: initialData?.title || "",
    description: initialData?.description || "",
    type: (initialData?.type ?? "CALL") as TaskReqType["type"],
    dueDate: initialData?.dueDate || "",
    contactId: initialData?.contactId || 0,
  });

  const handleChange = (
    key: keyof TaskReqType,
    value: string | number | undefined | null
  ) => {
    setForm((prev) => ({
      ...prev,
      [key]: value ?? undefined,
    }));
  };

  const handleSubmit = () => {
    onSubmit(form);
  };

  return (
    <div className="flex flex-col gap-4">

      <Input
        placeholder="Título"
        value={form.title}
        onChange={(e) => handleChange("title", e.target.value)}
      />

      <Input
        placeholder="Descripción"
        value={form.description || ""}
        onChange={(e) => handleChange("description", e.target.value)}
      />

      <Select
        value={form.type || ""}
        onValueChange={(value) => handleChange("type", value)}
      >
        <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
          <SelectValue placeholder="Tipo de tarea" />
        </SelectTrigger>
        <SelectContent className="bg-white">
          <SelectItem value="CALL">Llamada</SelectItem>
          <SelectItem value="EMAIL">Email</SelectItem>
          <SelectItem value="MEETING">Reunión</SelectItem>
          <SelectItem value="DEMO">Demo</SelectItem>
          <SelectItem value="OTHER">Otro</SelectItem>
        </SelectContent>
      </Select>

      <Input
        type="date"
        value={form.dueDate || ""}
        onChange={(e) => handleChange("dueDate", e.target.value)}
      />

      <Select
        value={form.contactId ? String(form.contactId) : ""}
        onValueChange={(value) =>
          handleChange("contactId", value ? Number(value) : undefined)
        }
      >
        <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
          <SelectValue placeholder="Seleccionar contacto" />
        </SelectTrigger>
        <SelectContent className="bg-white">
          {contacts.map((c) => (
            <SelectItem key={c.id} value={String(c.id)}>
              {c.name}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <div className="flex justify-end gap-2 pt-2 w-full">
        <Button variant="outline" onClick={onCancel} className="w-1/2">
          Cancelar
        </Button>
        <Button onClick={handleSubmit} className="w-1/2">
          Guardar tarea
        </Button>
      </div>
    </div>
  );
};