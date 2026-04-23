import { Search, Menu } from 'lucide-react';
import { Input } from '../ui/Input';
import { useAuthStore } from '../../store/useAuthStore';
import { getInitials } from '../../utils/getInitials';
import { useLocation } from "react-router-dom";
import { ROUTES } from '../../constants/routes';

// Definimos que el Header va a recibir una función para abrir el menú
interface HeaderProps {
  onMenuClick: () => void;
}

const routeTitles: Record<string, string> = {
  [ROUTES.CONTACTS]: "Contactos",
  [ROUTES.MESSAGES]: "Mis conversaciones",
  [ROUTES.TASKS]: "Mis tareas",
  [ROUTES.METRICS]: "Métricas",
};

export const Header = ({ onMenuClick }: HeaderProps) => {

  const { user } = useAuthStore()
  const location = useLocation();

  const section = location.pathname.split("/")[2];

  const currentTitle = routeTitles[section] || "Dashboard";

  return (
    <header className="grid grid-cols-[3fr_1fr] md:grid-cols-[1fr_1fr] items-center justify-content px-4 md:px-8 py-4 bg-white border-b border-slate-100">

      <div className="flex items-center gap-3 w-full md:w-auto">

        {/* Mobile: menú hamburguesa */}
        <button
          onClick={onMenuClick}
          aria-label="Abrir menú"
          className="md:hidden p-2 -ml-2 text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
        >
          <Menu size={24} />
        </button>

        {/* Mobile: título */}
        <h1 className="block md:hidden text-xl font-bold text-[#13316b]">
          {currentTitle}
        </h1>

        {/* Desktop: buscador */}
        <div className="hidden md:flex ml-2 w-full">
          <div className="relative w-full">
            <Search
              size={16}
              className="absolute left-3 top-1/2 -translate-y-1/2 text-neutro-2"
            />
            <Input
              type="text"
              placeholder="Buscar"
              className="pl-9 pr-4 py-2 border border-slate-200 rounded-lg text-sm w-full outline-none focus:border-[#3b82f6] transition-colors"
            />
          </div>
        </div>

      </div>

      {/* Lado Derecho: Controles */}
      <div className="flex items-center gap-4 md:gap-6 justify-self-end">
        <div className="flex items-center gap-3 md:gap-4 lg:pl-6 lg:border-l border-slate-200">
          <div className="flex items-center gap-2 md:gap-3">
            {/* Nombre oculto en móvil muy pequeño */}
            <span className="hidden sm:block text-sm font-medium text-slate-700">{user?.name}</span>
            <span className="w-10 h-10 bg-neutro-3 rounded-full flex items-center justify-center text-primary text-sm font-bold">
              {getInitials(user?.name)}
            </span>
          </div>
        </div>
      </div>
    </header>
  );
};