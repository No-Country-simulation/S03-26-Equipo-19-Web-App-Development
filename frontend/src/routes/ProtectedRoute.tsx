import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';


interface ProtectedRouteProps {
  children: React.ReactNode;
  redirectTo?: string;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children, 
  redirectTo = '/login' 
}) => {
  const { isAuthenticated, loading } = useAuthStore()

  console.log({isAuthenticated});
  
  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="animate-pulse flex flex-col items-center">
          <div className="w-12 h-12 bg-indigo-600 rounded-full mb-4"></div>
          <p className="text-slate-400 font-medium">Cargando...</p>
        </div>
      </div>
    );
  }

  //return isAuthenticated ? <>{children}</> : <Navigate to={redirectTo} replace />;
  return <>{children}</>;
};

export default ProtectedRoute;
