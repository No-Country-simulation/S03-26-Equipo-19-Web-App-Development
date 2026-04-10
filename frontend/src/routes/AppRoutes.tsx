import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { ROUTES } from "../constants/routes";
import PublicRoute from "../routes/PublicRoute"
import Login from "../pages/Login";

import ProtectedRoute from "./ProtectedRoute";
import Register from "../pages/Register";
import { DashboardPage } from "../pages/DashboardPage";
import { ContactsPage } from "../pages/ContactsPage";
import { MessagesPage } from "../pages/MessagesPage";
import { MetricsPage } from "../pages/MetricsPage";
import { useAuthStore } from "../store/useAuthStore";
import Home from "../components/dashboard/Home";
import ContactDetailPage from "../pages/ContactDetailPage";
import TasksPage from "../pages/TasksPage";
import SavedViewsPage from "../pages/SavedViewsPage";



const RootRedirect: React.FC = () => {
    const { isAuthenticated } = useAuthStore()

    return (
        <Navigate
            to={isAuthenticated ? ROUTES.DASHBOARD : ROUTES.LOGIN}
            replace
        />
    );
};

export const AppRoutes: React.FC = () => {
    return (
        <Routes>
            <Route
                path={ROUTES.LOGIN}
                element={
                    <PublicRoute>
                        <Login />
                    </PublicRoute>
                }
            />
            <Route
                path={ROUTES.REGISTER}
                element={
                    <PublicRoute>
                        <Register />
                    </PublicRoute>
                }
            />
            <Route
                path={ROUTES.DASHBOARD}
                element={
                    <ProtectedRoute>
                        <DashboardPage />
                    </ProtectedRoute>
                }
            >
                <Route index element={<Home />} />
                <Route path={ROUTES.CONTACTS} element={<ContactsPage />} />
                <Route path={ROUTES.CONTACT_DETALLE} element={<ContactDetailPage/>} />
                <Route path={ROUTES.MESSAGES} element={<MessagesPage />} />
                <Route path={ROUTES.METRICS} element={<MetricsPage />} />
                <Route path={ROUTES.TASKS} element={<TasksPage />}/>
                <Route path={ROUTES.SAVED_VIEWS} element={<SavedViewsPage />}/>
                


            </Route>

            <Route path={ROUTES.HOME} element={<RootRedirect />} />
            <Route path="/index.html" element={<RootRedirect />} />

        </Routes>
    );
};

export default AppRoutes;
