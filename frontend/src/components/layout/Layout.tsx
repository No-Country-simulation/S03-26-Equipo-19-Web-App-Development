import React from "react";
import Sidebar from "./Sidebar";
import Navbar from "./Navbar";

interface LayoutProps {
  children: React.ReactNode;
  onNavigate: (view: "landing" | "dashboard") => void;
  currentView: "landing" | "dashboard";
  onOpenModal: () => void;
  showSidebar?: boolean;
}

const Layout: React.FC<LayoutProps> = ({
  children,
  onNavigate,
  currentView,
  onOpenModal,
  showSidebar = true,
}) => {
  return (
    <div className="min-h-screen bg-[#F8FAFC] flex font-sans">
      {showSidebar && currentView === "dashboard" && (
        <Sidebar onNavigate={onNavigate} currentView={currentView} />
      )}
      <main className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {currentView === "dashboard" && <Navbar onOpenModal={onOpenModal} />}
        {children}
      </main>
    </div>
  );
};

export default Layout;
