import { useState } from 'react';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { Outlet } from 'react-router-dom';

export const DashboardLayout = () => {
  // Estado para controlar el menú en versión móvil
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="flex h-screen bg-[#f8fafc] font-sans overflow-hidden relative">

      {/* Overlay oscuro para móvil (se muestra si el sidebar está abierto) */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-slate-900/50 z-20 md:hidden transition-opacity"
          onClick={() => setSidebarOpen(false)}
          aria-hidden="true"
        />
      )}

      {/* Contenedor del Sidebar (Flotante en móvil, Fijo en escritorio) */}
      <div className={`fixed inset-y-0 left-0 transform ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} md:relative md:translate-x-0 transition duration-300 ease-in-out z-30`}>
        <Sidebar />
      </div>

      {/* Contenedor Principal */}
      <div className="flex-1 flex flex-col overflow-hidden w-full">
        {/* Pasamos la función al Header para que el botón hamburguesa la ejecute */}
        <Header onMenuClick={() => setSidebarOpen(true)} />

        {/* Padding responsivo: p-4 en móvil, p-8 en escritorio */}
        <main className="flex-1 overflow-y-auto p-4 md:p-8">
          <Outlet />
        </main>
      </div>

    </div>
  );
};