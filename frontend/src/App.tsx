import React from "react";
import { BrowserRouter } from "react-router-dom";
import AppRoutes from "./routes/AppRoutes";
import LoadingSpinner from "./components/ui/LoadingSpinner";
import { useAuthStore } from "./store/useAuthStore";


const AppContent: React.FC = () => {
  const { loading } = useAuthStore()

  if (loading) {
    return <LoadingSpinner message="Inicializando aplicación..." />;
  }

  return (
    <AppRoutes />
  );
};

export const App: React.FC = () => {
  return (
    <BrowserRouter>  
        <AppContent />
    </BrowserRouter>
  );
};

export default App;