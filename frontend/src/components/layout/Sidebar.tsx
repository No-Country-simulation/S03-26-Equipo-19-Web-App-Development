import {
  LayoutDashboard,
  Users,
  MessagesSquare,
  ListTodo,
  Bookmark,
  FileText,
  UserRoundCog,
 
  ChartNoAxesCombined,
  Tag,
  Download,
  LogOut,
} from "lucide-react";
import { ROUTES } from "../../constants/routes";
import { useAuthStore } from "../../store/useAuthStore";
import { useLocation, useNavigate } from "react-router-dom";


const sidebarItemsSales = [
  { icon: LayoutDashboard, path: ROUTES.DASHBOARD },
  { icon: Users, path: `/dashboard/${ROUTES.CONTACTS}` },
  { icon: MessagesSquare, path: `/dashboard/${ROUTES.MESSAGES}` },
  { icon: ListTodo, path: `/dashboard/${ROUTES.TASKS}` },
  { icon: Bookmark, path: `/dashboard/${ROUTES.SAVED_VIEWS}` }
];

const sidebarItemsAdmin = [
  { icon: LayoutDashboard, path: ROUTES.ADMIN_DASHBOARD },
  { icon: UserRoundCog, path: `/dashboard/admin/${ROUTES.CONTACTS}` },
  { icon: MessagesSquare, path: `/dashboard/admin/${ROUTES.MESSAGES}` },
  { icon: ListTodo, path: `/dashboard/admin/${ROUTES.TASKS}` },
  { icon: Bookmark, path: `/dashboard/admin/${ROUTES.SAVED_VIEWS}` },
  { icon: ChartNoAxesCombined, path: `/dashboard/admin/${ROUTES.METRICS}` },
  { icon: UserRoundCog, path: `/dashboard/admin/${ROUTES.SALESPERSONS}` },
  { icon: Tag, path: `/dashboard/admin/${ROUTES.TAGS}` },
  { icon: FileText, path: `/dashboard/admin/${ROUTES.TEMPLATES}` },
  { icon: Download, path: `/dashboard/admin/${ROUTES.REPORTS}` },
];


export const Sidebar = () => {
  const { user, logout } = useAuthStore();
  const location = useLocation();
  const navigate = useNavigate();

  //const [, , section] = location.pathname.split("/");

  const isActive = (path: string) => {
    return location.pathname === path;
  };

  const sidebarItems = user?.role === "ADMIN" ? sidebarItemsAdmin : sidebarItemsSales;
  
  return (
    <aside className="w-20 h-screen bg-[#13316b] flex flex-col items-center py-4 shadow-lg z-10">

      <span className="mb-8">
        <img src="/logo.svg" alt="Logo" className="w-12 h-12" />
      </span>

      <nav className="flex flex-col gap-2 w-full items-center">
        
        {sidebarItems.map((item, index) => {
          const Icon = item.icon;
          const active = isActive(item.path);

          return (
            <div
              key={index}
              className={`p-3 rounded-xl cursor-pointer transition-all ${active
                ? "bg-secondary text-white shadow-sm"
                : "text-secondary hover:text-white"
                }`}
              onClick={() => navigate(item.path)}
            >
              <Icon size={24} />
            </div>
          );
        })}
      </nav>

      <div className="mt-auto" onClick={() => logout()}>
        <LogOut
          size={24}
          className="text-secondary hover:text-white cursor-pointer transition-colors"
        />
      </div>

    </aside>
  );
};
