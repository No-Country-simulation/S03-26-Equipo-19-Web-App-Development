import React from "react";
import { Search, Bell, Zap } from "lucide-react";

interface NavbarProps {
  onOpenModal?: () => void;
}

const Navbar: React.FC<NavbarProps> = () => {
  return (
    <header className="h-20 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 flex items-center justify-between px-6 md:px-10 shrink-0 sticky top-0 z-40">
      <div className="flex items-center gap-4 flex-1">
        <div className="relative w-full max-w-md hidden md:block">
          <Search
            className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
            size={18}
          />
          <input
            type="text"
            placeholder="Buscar contactos, correos o empresas..."
            className="w-full pl-12 pr-4 py-3 bg-slate-50/50 border border-slate-200 rounded-2xl text-sm outline-none focus:ring-2 focus:ring-indigo-500/20 focus:bg-white transition-all font-medium"
          />
        </div>
        <div className="lg:hidden font-bold flex items-center gap-2">
          <div className="bg-gradient-to-br from-indigo-500 to-purple-500 p-1.5 rounded-lg text-white">
            <Zap size={14} fill="currentColor" />
          </div>
          Nexus
        </div>
      </div>

      <div className="flex items-center gap-4">
        <button className="relative p-3 text-slate-400 hover:bg-slate-50 hover:text-slate-900 rounded-xl transition-all group">
          <Bell
            size={20}
            className="group-hover:scale-110 transition-transform"
          />
          <span className="absolute top-2 right-2 w-2 h-2 bg-red-500 rounded-full animate-pulse" />
        </button>
        <div className="w-11 h-11 rounded-2xl bg-gradient-to-br from-indigo-400 to-purple-500 border-3 border-white shadow-lg overflow-hidden hover:scale-105 transition-transform cursor-pointer">
          <div className="w-full h-full flex items-center justify-center text-white font-bold">
            N
          </div>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
