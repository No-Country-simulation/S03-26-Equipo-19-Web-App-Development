import React, { useEffect } from "react";
import { X } from "lucide-react";
import { cn } from "../../lib/utils";

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  children: React.ReactNode;
  className?: string;
}

export function Modal({ isOpen, onClose, children, className }: ModalProps) {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "unset";
    }
    return () => {
      document.body.style.overflow = "unset";
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center p-6 bg-slate-900/60 backdrop-blur-md animate-fade-in"
      onClick={onClose}
    >
      <div
        className={cn(
          "bg-white w-full max-w-lg rounded-[2.5rem] shadow-2xl p-8 md:p-10 animate-scale-up relative overflow-hidden",
          className,
        )}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Modal Background Decoration */}
        <div className="absolute top-0 right-0 w-64 h-64 bg-gradient-to-br from-indigo-500/10 to-purple-500/10 rounded-full blur-3xl" />
        {children}
      </div>
    </div>
  );
}

interface ModalHeaderProps {
  onClose: () => void;
  children: React.ReactNode;
}

export function ModalHeader({ onClose, children }: ModalHeaderProps) {
  return (
    <div className="flex justify-between items-start mb-8 relative z-10">
      {children}
      <button
        onClick={onClose}
        className="p-3 bg-slate-50 text-slate-400 rounded-xl hover:text-slate-900 hover:bg-slate-100 transition-all"
      >
        <X size={22} />
      </button>
    </div>
  );
}
