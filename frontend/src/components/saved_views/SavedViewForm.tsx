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
import { getStatusLabel } from "../../utils/formateStatusLabel";
import { useAuthStore } from "../../store/useAuthStore";
import { useGetSalespersons } from "../../services/use_queries/salespersons-query";



export type SavedViewForm = {
  name: string;
  entity: "CONTACTS" | "TASKS";
  filters: Record<string, any>;
  sortBy?: string;
  sortOrder?: "ASC" | "DESC";
  global: boolean;
};

type Props = {
  initialValues?: SavedViewForm;
  onSubmit: (data: SavedViewForm) => void;
  onCancel: () => void;
};

const CONTACT_STATUS = [
  "NEW_LEAD",
  "CONTACTED",
  "IN_NEGOTIATION",
  "PROPOSAL_SENT",
  "CLOSED_WON",
  "CLOSED_LOST",
];

const TASK_STATUS = ["PENDING", "COMPLETED", "OVERDUE"];

const getTaskStatusLabel = (status: string) => {
  switch (status) {
    case "PENDING":
      return "Pendiente";
    case "COMPLETED":
      return "Completada";
    case "OVERDUE":
      return "Vencida";
    default:
      return status;
  }
};

export const SavedViewForm = ({
  initialValues,
  onSubmit,
  onCancel
}: Props) => {

  const { user } = useAuthStore();
  const isAdmin = user?.role === "ADMIN";

  const { data: salespersons, isLoading } = useGetSalespersons(isAdmin)

  const [form, setForm] = useState<SavedViewForm>({
    name: initialValues?.name || "",
    entity: initialValues?.entity || "CONTACTS",
    filters: initialValues?.filters || (isAdmin ? {} : { ownerId: user?.id }),
    sortBy: initialValues?.sortBy || undefined,
    sortOrder: initialValues?.sortOrder || "ASC",
    global: initialValues?.global || false,
  });


  const handleChange = (key: keyof SavedViewForm, value: any) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleFilterChange = (key: string, value: any) => {
    setForm((prev) => ({
      ...prev,
      filters: {
        ...prev.filters,
        [key]: value,
      },
    }));
  };

  const handleEntityChange = (value: "CONTACTS" | "TASKS" | null) => {
    if (!value) return; // evita null

    setForm((prev) => ({
      ...prev,
      entity: value,
      filters: {},
      sortBy: undefined,
    }));
  };

  const handleSubmit = () => {
    const payload = {
      ...form,
      filters: {
        ...form.filters,
        ...(form.entity === "CONTACTS" && { ownerId: user?.id }),
      },
    };

    onSubmit(payload);
  };

  return (
    <div className="flex flex-col gap-4">

      <Input
        placeholder="Nombre de la vista"
        value={form.name}
        onChange={(e) => handleChange("name", e.target.value)}
      />

      <Select value={form.entity} onValueChange={handleEntityChange}>
        <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
          <SelectValue placeholder="Seleccionar entidad" />
        </SelectTrigger>
        <SelectContent className={"bg-white"}>
          <SelectItem value="CONTACTS">Contactos</SelectItem>
          <SelectItem value="TASKS">Tareas</SelectItem>
        </SelectContent>
      </Select>

      {form.entity === "CONTACTS" && (
        <>
          <Select
            value={form.filters.funnelStatus || ""}
            onValueChange={(value) =>
              handleFilterChange(
                "funnelStatus",
                value === "" ? null : value
              )
            }
          >
            <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
              <SelectValue placeholder="Estado en funnel">
                {form.filters.funnelStatus
                  ? getStatusLabel(form.filters.funnelStatus)
                  : "Estado en funnel"}
              </SelectValue>
            </SelectTrigger>

            <SelectContent className={"bg-white"}>
              {CONTACT_STATUS.map((status) => (
                <SelectItem key={status} value={status}>
                  {getStatusLabel(status)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <div className="flex flex-col gap-1">
            <span className="text-xs text-slate-400">Vendedor asignado</span>

            {isAdmin ? (
              <Select
                value={
                  form.filters.ownerId
                    ? String(form.filters.ownerId)
                    : "ALL"
                }
                onValueChange={(value) =>
                  handleFilterChange(
                    "ownerId",
                    value === "ALL" ? null : Number(value)
                  )
                }
              >
                <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
                  <SelectValue placeholder="Seleccionar vendedor" />
                </SelectTrigger>

                <SelectContent>
                  <SelectItem value="ALL">Todos</SelectItem>
                  {isLoading ? (
                    <div className="px-3 py-2 text-sm text-slate-400">
                      Cargando vendedores...
                    </div>
                  ) : (
                    salespersons?.map((u) => (
                      <SelectItem key={u.id} value={String(u.id)}>
                        {u.name}
                      </SelectItem>
                    ))
                  )}
                </SelectContent>
              </Select>
            ) : (
              <div className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-3 rounded-md text-sm">
                {user?.name}
              </div>
            )}
          </div>
        </>
      )}

      {form.entity === "TASKS" && (
        <>
          <Select
            value={form.filters.status || ""}
            onValueChange={(value) => handleFilterChange("status", value)}
          >
            <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
              <SelectValue placeholder="Estado">
                {form.filters.status
                  ? getTaskStatusLabel(form.filters.status)
                  : "Estado"}
              </SelectValue>
            </SelectTrigger>

            <SelectContent className={"bg-white"}>
              <SelectItem value="">Todos</SelectItem>
              {TASK_STATUS.map((status) => (
                <SelectItem key={status} value={status}>
                  {getTaskStatusLabel(status)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Input
            type="date"
            value={form.filters.dueDateFrom || ""}
            onChange={(e) =>
              handleFilterChange("dueDateFrom", e.target.value || undefined)
            }
          />
        </>
      )}

      {/* SORT BY */}
      <Select
        value={form.sortBy || ""}
        onValueChange={(value) =>
          handleChange("sortBy", value === "" ? undefined : value)
        }
      >
        <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
          <SelectValue placeholder="Ordenar por" />
        </SelectTrigger>
        <SelectContent className={"bg-white"}>
          {form.entity === "CONTACTS" ? (
            <>
              <SelectItem value="createdAt">Fecha creación</SelectItem>
              <SelectItem value="name">Nombre</SelectItem>
              <SelectItem value="funnelStatus">Funnel</SelectItem>
            </>
          ) : (
            <>
              <SelectItem value="dueDate">Fecha vencimiento</SelectItem>
              <SelectItem value="status">Estado</SelectItem>
              <SelectItem value="createdAt">Fecha creación</SelectItem>
            </>
          )}
        </SelectContent>
      </Select>

      {/* SORT ORDER */}
      <Select
        value={form.sortOrder}
        onValueChange={(value) => handleChange("sortOrder", value)}
      >
        <SelectTrigger className="w-full bg-white border border-neutro-2 text-neutro-1 shadow-sm px-4 py-5">
          <SelectValue placeholder="Orden" />
        </SelectTrigger>
        <SelectContent className={"bg-white"}>
          <SelectItem value="ASC">Ascendente</SelectItem>
          <SelectItem value="DESC">Descendente</SelectItem>
        </SelectContent>
      </Select>

      {/* GLOBAL */}
      {isAdmin && (<label className="flex items-center gap-2 text-sm text-slate-600">
        <input
          type="checkbox"
          checked={form.global}
          onChange={(e) => handleChange("global", e.target.checked)}
          className="accent-primary"
        />
        Vista global
      </label>)}


      {/* ACTIONS */}
      <div className="flex justify-end gap-2 pt-2 w-full">
        <Button variant="outline" onClick={onCancel} className="w-1/2">
          Cancelar
        </Button>
        <Button onClick={handleSubmit} className="w-1/2">
          Guardar vista
        </Button>
      </div>
    </div>
  );
};