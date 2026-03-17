import { Search, Bell, Menu } from 'lucide-react';

// Definimos que el Header va a recibir una función para abrir el menú
interface HeaderProps {
  onMenuClick: () => void;
}

export const Header = ({ onMenuClick }: HeaderProps) => {
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

        <div>
          {/* Breadcrumbs (Ocultos en móvil) */}
          <div className="hidden md:flex items-center gap-2 text-sm font-medium mb-1">
            <span className="text-[#13316b] font-bold">CRM Dashboard</span>
            <span className="text-slate-400">/</span>
            <span className="text-slate-600">CoreCRM Startup</span>
          </div>
          <h1 className="text-xl md:text-2xl font-bold text-[#13316b]">Dashboard</h1>
        </div>
      </div>

      {/* Lado Derecho: Controles */}
      <div className="flex items-center gap-4 md:gap-6">
        
        {/* Selector y Buscador (Ocultos en pantallas pequeñas para ahorrar espacio) */}
        <div className="hidden lg:flex items-center gap-6">
          <select 
            aria-label="Seleccionar periodo de tiempo"
            className="border border-slate-200 bg-white px-3 py-2 rounded-lg text-sm font-medium text-slate-600 outline-none cursor-pointer"
          >
            <option>Last 30 Days</option>
          </select>

          <div className="relative">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input 
              type="text" 
              placeholder="Search" 
              className="pl-9 pr-4 py-2 border border-slate-200 rounded-lg text-sm w-64 outline-none focus:border-[#3b82f6] transition-colors"
            />
          </div>
        </div>

        {/* Perfil y Notificaciones (Siempre visibles) */}
        <div className="flex items-center gap-3 md:gap-4 lg:pl-6 lg:border-l border-slate-200">
          <button 
            aria-label="Ver notificaciones"
            className="relative p-1 text-slate-400 hover:text-slate-600"
          >
            <Bell size={20} />
            <span className="absolute top-0 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
          </button>
          
          <div className="flex items-center gap-2 md:gap-3">
            {/* Nombre oculto en móvil muy pequeño */}
            <span className="hidden sm:block text-sm font-medium text-slate-700">Ana García</span>
            <img 
              src="https://i.pravatar.cc/150?img=32" 
              alt="Avatar" 
              className="w-8 h-8 rounded-full object-cover border border-slate-200"
            />
          </div>
        </div>

      </div>
    </header>
  );
};