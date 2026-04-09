import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { ROUTES } from "../constants/routes";
import PublicRoute from "../routes/PublicRoute";
import Login from "../pages/Login";
import ProtectedRoute from "./ProtectedRoute";
import Register from "../pages/Register";
import { DashboardPage } from "../pages/DashboardPage";
import { ContactsPage } from "../pages/ContactsPage";
import { MessagesPage } from "../pages/MessagesPage";
import { useAuthStore } from "../store/useAuthStore";
import Home from "../components/dashboard/Home";
import ContactDetailPage from "../pages/ContactDetailPage";

// Admin pages
import { AdminPanel } from "../pages/admin/AdminPanel";
import { UsersManagement } from "../pages/admin/UsersManagement";
import { Funnels } from "../pages/admin/Funnels";
import { TagsManagement } from "../pages/admin/TagsManagement";
import { EmailTemplates } from "../pages/admin/EmailTemplates";
import { ExportsManagement } from "../pages/admin/ExportsManagement";
import { MetricsPage } from "../pages/admin/MetricsPage";
import { Conversations } from "../pages/admin/Conversations";
import { TasksManagement } from "../pages/admin/TasksManagement";

// 1. Modificamos el redirect para que evalúe el rol del usuario
const RootRedirect: React.FC = () => {
    const { isAuthenticated, user } = useAuthStore();
    
    if (!isAuthenticated) {
        return <Navigate to={ROUTES.LOGIN} replace />;
    }

    // Si es ADMIN va a su ruta, si no, al dashboard de ventas
    return <Navigate to={user?.role === 'ADMIN' ? ROUTES.ADMIN : ROUTES.DASHBOARD} replace />;
};

export const AppRoutes: React.FC = () => {
    const { user } = useAuthStore();
    const isAdmin = user?.role === 'ADMIN';

    return (
        <Routes>
            {/* Rutas Públicas */}
            <Route path={ROUTES.LOGIN} element={<PublicRoute><Login /></PublicRoute>} />
            <Route path={ROUTES.REGISTER} element={<PublicRoute><Register /></PublicRoute>} />

            {/* ÚNICO bloque para /dashboard */}
            <Route
                path="/dashboard" 
                element={
                    <ProtectedRoute>
                        <DashboardPage />
                    </ProtectedRoute>
                }
            >
                {/* 1. Index dinámico según rol */}
                <Route index element={isAdmin ? <AdminPanel /> : <Home />} />

                {/* 2. Rutas que AMBOS comparten */}
                <Route path={ROUTES.CONTACTS} element={<ContactsPage />} />
                <Route path={ROUTES.CONTACT_DETALLE} element={<ContactDetailPage/>} />
                <Route path={ROUTES.MESSAGES} element={<MessagesPage />} />
                <Route path={ROUTES.TASKS} element={<TasksManagement />} />

                {/* 3. Rutas que SOLO ve el admin bajo /dashboard/xxx */}
                {isAdmin && (
                    <>
                        <Route path={ROUTES.USERS} element={<UsersManagement />} />
                        <Route path={ROUTES.FUNNEL} element={<Funnels />} />
                        <Route path={ROUTES.METRICS} element={<MetricsPage />} />
                        <Route path={ROUTES.TAGS} element={<TagsManagement />} />
                        <Route path={ROUTES.TEMPLATES} element={<EmailTemplates />} />
                        <Route path={ROUTES.EXPORTS} element={<ExportsManagement />} />
                        <Route path={ROUTES.CONVERSATIONS} element={<Conversations />} />
                    </>
                )}
            </Route>

            {/* Redirecciones */}
            <Route path={ROUTES.HOME} element={<RootRedirect />} />
            <Route path="*" element={<RootRedirect />} />
        </Routes>
    );
};

export default AppRoutes;