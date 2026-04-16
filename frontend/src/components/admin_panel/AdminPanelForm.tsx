import { useState } from "react";
import { Button } from "../ui/Button";
import { Input } from "../ui/Input";

export type AdminPanelFormData = {
  name: string;
  description: string;
};

type Props = {
  initialValues?: AdminPanelFormData;
  onSubmit: (data: AdminPanelFormData) => void;
  onCancel: () => void;
};

export const AdminPanelForm = ({
  initialValues,
  onSubmit,
  onCancel,
}: Props) => {
  const [form, setForm] = useState<AdminPanelFormData>({
    name: initialValues?.name || "",
    description: initialValues?.description || "",
  });

  const handleChange = (key: keyof AdminPanelFormData, value: string) => {
    setForm((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const handleSubmit = () => {
    onSubmit(form);
  };

  return (
    <div className="flex flex-col gap-4">

      <Input
        placeholder="Nombre"
        value={form.name}
        onChange={(e) => handleChange("name", e.target.value)}
      />

      <Input
        placeholder="Descripción"
        value={form.description}
        onChange={(e) => handleChange("description", e.target.value)}
      />

      <div className="flex justify-end gap-2 pt-2 w-full">
        <Button variant="outline" onClick={onCancel} className="w-1/2">
          Cancelar
        </Button>
        <Button onClick={handleSubmit} className="w-1/2">
          Crear
        </Button>
      </div>
    </div>
  );
};