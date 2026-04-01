import { NavLink } from 'react-router-dom';
import { LayoutGrid, Users, MessageSquare, BarChart2, Settings } from 'lucide-react';

const links = [
  { to: '/dashboard',          icon: <LayoutGrid size={22} />,    label: 'Dashboard' },
  { to: '/dashboard/contacts', icon: <Users size={22} />,         label: 'Contactos' },
  { to: '/dashboard/messages', icon: <MessageSquare size={22} />, label: 'Mensajes' },
  { to: '/dashboard/metrics',  icon: <BarChart2 size={22} />,     label: 'Métricas' },
];

export const Sidebar = () => {
  return (
    <aside className="w-20 h-screen bg-[#13316b] flex flex-col items-center py-6 shadow-lg z-10">
      
      <div className="w-10 h-10 bg-[#3b82f6] rounded-xl flex items-center justify-center text-white font-bold text-2xl mb-8">
        C
      </div>
      
      <nav className="flex flex-col gap-6 w-full items-center">
        <div className="p-3 bg-[#2563eb] rounded-xl text-white cursor-pointer shadow-sm">
          <LayoutGrid size={22} />
        </div>
        
        {/* Íconos inactivos atenuados para mayor contraste */}
        <Users size={22} className="text-[#7ea6e0] hover:text-white cursor-pointer transition-colors" />
        <MessageSquare size={22} className="text-[#7ea6e0] hover:text-white cursor-pointer transition-colors" />
        <Megaphone size={22} className="text-[#7ea6e0] hover:text-white cursor-pointer transition-colors" />
        <BarChart2 size={22} className="text-[#7ea6e0] hover:text-white cursor-pointer transition-colors" />
        <FileText size={22} className="text-[#7ea6e0] hover:text-white cursor-pointer transition-colors" />
      </nav>

      <div className="mt-auto">
        <NavLink
          to="/settings"
          title="Configuración"
          className={({ isActive }) =>
            isActive
              ? 'p-3 bg-[#2563eb] rounded-xl text-white shadow-sm'
              : 'p-3 text-[#7ea6e0] hover:text-white transition-colors'
          }
        >
          <Settings size={22} />
        </NavLink>
      </div>

    </aside>
  );
};