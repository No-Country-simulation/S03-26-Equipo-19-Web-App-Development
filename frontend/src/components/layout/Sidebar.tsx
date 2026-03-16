import React from "react";
import {
  Zap,
  LayoutDashboard,
  MessageSquare,
  Users,
  Settings,
} from "lucide-react";
import { cn } from "../../lib/utils";

interface SidebarProps {
  onNavigate: (view: "landing" | "dashboard") => void;
  currentView: "landing" | "dashboard";
}

const Sidebar: React.FC<SidebarProps> = ({ onNavigate, currentView }) => {
  const navItems = [
    {
      icon: LayoutDashboard,
      label: "Panel Principal",
      view: "dashboard" as const,
      active: currentView === "dashboard",
    },
    {
      icon: MessageSquare,
      label: "Conversaciones",
      view: "dashboard" as const,
      active: false,
    },
    {
      icon: Users,
      label: "Segmentación",
      view: "dashboard" as const,
      active: false,
    },
    {
      icon: Settings,
      label: "Ajustes",
      view: "dashboard" as const,
      active: false,
    },
  ];

  return (
    <aside className="hidden lg:flex w-72 bg-white border-r border-slate-200/60 flex-col p-6 sticky top-0 h-screen shadow-xl shadow-slate-200/50">
      <div
        className="flex items-center gap-3 font-black text-2xl mb-12 cursor-pointer"
        onClick={() => onNavigate("landing")}
      >
        <div className="bg-gradient-to-br from-indigo-500 via-purple-500 to-indigo-500 p-2 rounded-xl text-white shadow-lg shadow-indigo-500/30 hover:scale-110 transition-transform">
          <Zap size={20} fill="currentColor" />
        </div>
        <span className="bg-clip-text text-transparent bg-gradient-to-r from-slate-900 to-slate-600">
          Nexus
        </span>
      </div>

      <nav className="space-y-2 flex-1">
        {navItems.map((item, idx) => (
          <button
            key={idx}
            className={cn(
              "w-full flex items-center gap-3 px-4 py-3.5 rounded-2xl font-semibold text-sm transition-all group",
              item.active
                ? "bg-gradient-to-r from-indigo-500 to-purple-500 text-white font-bold shadow-lg shadow-indigo-500/25 hover:shadow-indigo-500/40"
                : "text-slate-500 hover:bg-slate-50 hover:text-slate-900",
            )}
          >
            <item.icon
              size={20}
              className="group-hover:scale-110 transition-transform"
            />
            {item.label}
          </button>
        ))}
      </nav>

      <div className="bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 rounded-3xl p-5 text-white shadow-xl">
        <div className="flex items-center justify-between mb-3">
          <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
            Plan Actual
          </div>
          <div className="px-2 py-1 rounded-full bg-indigo-500/20 text-[10px] font-bold text-indigo-400">
            PRO
          </div>
        </div>
        <div className="text-lg font-bold mb-4">Startup Pro v2.0</div>
        <div className="w-full h-2 bg-white/10 rounded-full overflow-hidden">
          <div className="bg-gradient-to-r from-indigo-500 to-purple-500 h-full w-2/3 rounded-full shadow-lg shadow-indigo-500/50" />
        </div>
        <div className="text-xs text-slate-400 mt-2">
          70% de tu plan utilizado
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
