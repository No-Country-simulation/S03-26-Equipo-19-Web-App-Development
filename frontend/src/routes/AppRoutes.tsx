// src/routes/AppRoutes.tsx
import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { ROUTES } from "../constants/routes";
import { useAuthStore } from "../store/useAuthStore";

// Layouts y Rutas Protegidas
import PublicRoute from "../routes/PublicRoute";
import ProtectedRoute from "./ProtectedRoute";
import { DashboardLayout } from "../components/layout/DashboardLayout";

// Páginas Públicas
import Login from "../pages/Login";
import Register from "../pages/Register";

// Páginas de Vendedor / Generales
import Home from "../components/dashboard/Home";
import { ContactsPage } from "../pages/ContactsPage"; 
import { MessagesPage } from "../pages/MessagesPage";
import ContactDetailPage from "../pages/ContactDetailPage";
import TasksPage from "../pages/TasksPage";

// Páginas de Admin
import { AdminPanel } from "../pages/admin/AdminPanel";
import { TagsManagement } from "../pages/admin/TagsManagement";
import { EmailTemplates } from "../pages/admin/EmailTemplates";
import { ExportsManagement } from "../pages/admin/ExportsManagement";
import { MetricsPage } from "../pages/admin/MetricsPage";
import { Conversations } from "../pages/admin/Conversations";
import { TasksManagement } from "../pages/admin/TasksManagement";
import { SalespersonsManagement } from "../pages/admin/SalespersonsManagement";
import { SavedViewsPage } from "../pages/admin/SavedViewsPage";

// Redirección inicial según rol
const RootRedirect: React.FC = () => {
  const { isAuthenticated, user } = useAuthStore();
  if (!isAuthenticated) return <Navigate to={ROUTES.LOGIN} replace />;
  return <Navigate to={user?.role === 'ADMIN' ? ROUTES.ADMIN_DASHBOARD : ROUTES.DASHBOARD} replace />;
};

export const AppRoutes: React.FC = () => {
  const { user } = useAuthStore();
  const isAdmin = user?.role === 'ADMIN';

  return (
    <Routes>
      {/* RUTAS PÚBLICAS */}
      <Route path={ROUTES.LOGIN} element={<PublicRoute><Login /></PublicRoute>} />
      <Route path={ROUTES.REGISTER} element={<PublicRoute><Register /></PublicRoute>} />

      {/* BLOQUE PRIVADO */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        {/* Al entrar a /dashboard, decidimos qué componente mostrar en el Outlet */}
        <Route 
          index 
          element={isAdmin ? <Navigate to="admin" replace /> : <Home />} 
        />

        {/* VISTA VENDEDOR (solo si NO es admin) */}
        {!isAdmin && (
          <>
            {/* 👈 USAMOS ContactsPage con isAdminView=false */}
            <Route path={ROUTES.CONTACTS} element={<ContactsPage isAdminView={false} />} />
            <Route path={ROUTES.MESSAGES} element={<MessagesPage />} />
            <Route path={ROUTES.TASKS} element={<TasksPage />}/>
            <Route path={ROUTES.SAVED_VIEWS} element={<SavedViewsPage />} />
          </>
        )}

        {/* VISTA ADMIN */}
        {isAdmin && (
          <Route path="admin">
            <Route index element={<AdminPanel />} />
            {/* 👈 USAMOS ContactsPage con isAdminView=true */}
            <Route path={ROUTES.CONTACTS} element={<ContactsPage isAdminView={true} />} />
            <Route path={ROUTES.MESSAGES} element={<Conversations />} />
            <Route path={ROUTES.TASKS} element={<TasksManagement />} />
            <Route path={ROUTES.SAVED_VIEWS} element={<SavedViewsPage />} />
            <Route path={ROUTES.METRICS} element={<MetricsPage />} />
            <Route path={ROUTES.SALESPERSONS} element={<SalespersonsManagement />} />
            <Route path={ROUTES.TAGS} element={<TagsManagement />} />
            <Route path={ROUTES.TEMPLATES} element={<EmailTemplates />} />
            <Route path={ROUTES.REPORTS} element={<ExportsManagement />} />
          </Route>
        )}
        
        {/* Ruta de detalle - se mantiene igual para ambos roles */}
        <Route path={ROUTES.CONTACT_DETALLE} element={<ContactDetailPage />} />
      </Route>

      <Route path={ROUTES.HOME} element={<RootRedirect />} />
      <Route path="*" element={<RootRedirect />} />
    </Routes>
  );
};

export default AppRoutes;