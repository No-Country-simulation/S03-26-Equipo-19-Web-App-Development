import { cn } from "../../lib/utils";
import { LeadStatus } from "../../types";

interface BadgeProps {
  status: LeadStatus;
  className?: string;
}

const statusStyles: Record<LeadStatus, string> = {
  "Lead Activo": "bg-indigo-50 text-indigo-700 border-indigo-100",
  "En Seguimiento": "bg-amber-50 text-amber-700 border-amber-100",
  "Cliente Cerrado": "bg-emerald-50 text-emerald-700 border-emerald-100",
};

export function Badge({ status, className }: BadgeProps) {
  return (
    <span
      className={cn(
        "px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider border",
        statusStyles[status],
        className,
      )}
    >
      {status}
    </span>
  );
}
