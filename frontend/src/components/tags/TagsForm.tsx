import { useState } from "react";
import { Button } from "../ui/Button";
import { Input } from "../ui/Input";
import type { TagRequest } from "../../types/admin.types";

type Props = {
  initialValues?: TagRequest;
  onSubmit: (data: TagRequest) => void;
  onCancel: () => void;
};

export const TagsForm = ({
  initialValues,
  onSubmit,
  onCancel,
}: Props) => {
  const [form, setForm] = useState<TagRequest>({
    name: initialValues?.name || "",
    color: initialValues?.color || "#3B82F6",
  });
  
  const handleChange = (key: keyof TagRequest, value: string) => {
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
        placeholder="Nombre del tag"
        value={form.name}
        onChange={(e) => handleChange("name", e.target.value)}
      />

      <Input
        type="color"
        value={form.color}
        onChange={(e) => handleChange("color", e.target.value)}
        className="h-10 p-1"
      />

      <div className="flex justify-end gap-2 pt-2 w-full">
        <Button variant="outline" onClick={onCancel} className="w-1/2">
          Cancelar
        </Button>
        <Button onClick={handleSubmit} className="w-1/2">
          Guardar tag
        </Button>
      </div>
    </div>
  );
};