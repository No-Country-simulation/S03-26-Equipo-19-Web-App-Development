// src/components/contacts/SalespersonSelector.tsx
import { useState } from "react";
import { useGetSalespersons } from "../../services/use_queries/salespersons-query";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../ui/select";
import { UserRoundCog, Check, X } from "lucide-react";

interface SalespersonSelectorProps {
  contactId: number;
  currentOwnerId?: number;
  currentOwnerName?: string;
  onAssign: (contactId: number, newOwnerId: number) => void;
  isAssigning?: boolean;
}

export const SalespersonSelector = ({
  contactId,
  currentOwnerName,
  onAssign,
  isAssigning = false,
}: SalespersonSelectorProps) => {
  const [isEditing, setIsEditing] = useState(false);
  const [selectedSalespersonId, setSelectedSalespersonId] = useState<string>("");
  
  const { data: salespersons, isLoading } = useGetSalespersons(true);

  const handleAssign = () => {
    if (selectedSalespersonId) {
      onAssign(contactId, parseInt(selectedSalespersonId));
      setIsEditing(false);
      setSelectedSalespersonId("");
    }
  };

  const handleCancel = () => {
    setIsEditing(false);
    setSelectedSalespersonId("");
  };

  const handleValueChange = (value: string | null) => {
    setSelectedSalespersonId(value || "");
  };

  // Modo edición
  if (isEditing) {
    if (isLoading) {
      return <span className="text-sm text-gray-400">Cargando...</span>;
    }

    return (
      <div className="flex items-center gap-2">
        <Select value={selectedSalespersonId} onValueChange={handleValueChange}>
          <SelectTrigger className="w-36 h-8 text-sm">
            <SelectValue placeholder="Seleccionar vendedor" />
          </SelectTrigger>
          <SelectContent className="bg-white">
            {salespersons?.map((salesperson) => (
              <SelectItem key={salesperson.id} value={salesperson.id.toString()}>
                {salesperson.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <button
          onClick={handleAssign}
          className="text-green-600 hover:text-green-800"
          disabled={!selectedSalespersonId || isAssigning}
        >
          <Check size={16} />
        </button>
        <button
          onClick={handleCancel}
          className="text-red-600 hover:text-red-800"
          disabled={isAssigning}
        >
          <X size={16} />
        </button>
      </div>
    );
  }

  // Modo visual - el nombre del vendedor es clickeable
  return (
    <div 
      onClick={() => setIsEditing(true)}
      className="flex items-center gap-2 cursor-pointer group w-full"
    >
      <span className="text-sm text-gray-700 group-hover:text-blue-600 transition-colors">
        {currentOwnerName || "Sin asignar"}
      </span>
      <UserRoundCog 
        size={14} 
        className="text-gray-400 group-hover:text-blue-600 opacity-0 group-hover:opacity-100 transition-all"
      />
    </div>
  );
};