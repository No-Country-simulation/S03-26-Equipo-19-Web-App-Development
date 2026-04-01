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
    <header className="flex justify-between items-center px-4 md:px-8 py-4 bg-white border-b border-slate-100">

      {/* Lado Izquierdo: Menú Hamburguesa (Solo Móvil) + Títulos */}
      <div className="flex items-center gap-3 md:gap-0">
        <button
          onClick={onMenuClick}
          aria-label="Abrir menú"
          className="md:hidden p-2 -ml-2 text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
        >
          <Menu size={24} />
        </button>

        <h1 className="text-xl md:text-2xl font-bold text-[#13316b]">{currentTitle}</h1>

      </div>

      {/* Lado Derecho: Controles */}
      <div className="flex items-center gap-4 md:gap-6">

        {/* Selector y Buscador (Ocultos en pantallas pequeñas para ahorrar espacio) */}
        <div className="hidden lg:flex items-center gap-6">

          <div className="relative">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <Input
              type="text"
              placeholder="Buscar"
              className="pl-9 pr-4 py-2 border border-slate-200 rounded-lg text-sm w-64 outline-none focus:border-[#3b82f6] transition-colors"
            />
          </div>
        </div>

        {/* Perfil y Notificaciones (Siempre visibles) */}
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