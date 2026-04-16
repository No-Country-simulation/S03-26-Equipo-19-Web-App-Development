import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "../ui/Button";

interface Props {
  page: number;
  totalPages: number;
  onNext: () => void;
  onPrev: () => void;
}

export const PaginationControls = ({
  page,
  totalPages,
  onNext,
  onPrev,
}: Props) => {
  return (
    <div className="flex justify-center items-center gap-4 mt-8">
      <Button variant="ghost"disabled={page === 1} onClick={onPrev}>
        <ChevronLeft className="h-4 w-4" />
      </Button>

      <span className="text-sm text-slate-500">
        Página {page} de {totalPages || 1}
      </span>

      <Button variant="ghost" disabled={page >= totalPages} onClick={onNext}>
        <ChevronRight className="h-4 w-4" />
      </Button>
    </div>
  );
};