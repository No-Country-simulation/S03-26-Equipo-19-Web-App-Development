import { LayoutGrid, Users, MessageSquare, Megaphone, BarChart2, FileText, Settings } from 'lucide-react';

export const Sidebar = () => {
  return (
    <aside className="w-20 h-screen bg-[#13316b] flex flex-col items-center py-6 shadow-lg z-10">
      
      
      
      <nav className="flex flex-col gap-6 w-full items-center">
        <div className="p-3 bg-secondary rounded-xl text-white cursor-pointer shadow-sm">
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
        <Settings size={22} className="text-[#7ea6e0] hover:text-white cursor-pointer transition-colors" />
      </div>
    </aside>
  );
};