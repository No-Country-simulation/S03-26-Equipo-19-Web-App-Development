import React from "react";
import { Zap, Plus } from "lucide-react";
import { Modal, ModalHeader } from "../ui/Modal";
import { Input, Select } from "../ui/Input";
import { Button } from "../ui/Button";
import { LeadFormData, LeadStatus, Channel } from "../../types";

interface LeadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: LeadFormData) => void;
}

const LeadModal: React.FC<LeadModalProps> = ({ isOpen, onClose, onSubmit }) => {
  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const data: LeadFormData = {
      name: formData.get("name") as string,
      email: formData.get("email") as string,
      status: formData.get("status") as LeadStatus,
      channel: formData.get("channel") as Channel,
    };
    onSubmit(data);
    onClose();
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <ModalHeader onClose={onClose}>
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-50 text-indigo-600 text-xs font-bold mb-3">
            <Plus size={14} /> NUEVO
          </div>
          <h3 className="text-3xl font-black text-slate-900 mb-1">
            Nuevo Lead
          </h3>
          <p className="text-slate-400 text-sm">
            Completa la información para sincronizar con la nube.
          </p>
        </div>
      </ModalHeader>

      <form onSubmit={handleSubmit} className="space-y-6 relative z-10">
        <div className="grid grid-cols-1 gap-6">
          <Input
            name="name"
            label="Nombre Completo"
            placeholder="Ej. Sofia Rodriguez"
            required
            autoFocus
          />
          <Input
            name="email"
            type="email"
            label="Email Corporativo"
            placeholder="sofia@empresa.com"
            required
          />
          <div className="grid grid-cols-2 gap-4">
            <Select
              name="channel"
              label="Canal"
              options={[
                { value: "WhatsApp", label: "WhatsApp" },
                { value: "Email", label: "Email" },
              ]}
            />
            <Select
              name="status"
              label="Estado"
              options={[
                { value: "Lead Activo", label: "Lead Activo" },
                { value: "En Seguimiento", label: "En Seguimiento" },
                { value: "Cliente Cerrado", label: "Cliente Cerrado" },
              ]}
            />
          </div>
        </div>

        <div className="pt-6 flex gap-4">
          <Button
            type="button"
            variant="outline"
            onClick={onClose}
            className="flex-1"
          >
            Cancelar
          </Button>
          <Button type="submit" className="flex-1">
            <Zap size={18} fill="currentColor" />
            Guardar en Nube
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default LeadModal;
