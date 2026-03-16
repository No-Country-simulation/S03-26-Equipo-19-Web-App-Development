import { useState } from "react";
import LandingPage from "./pages/LandingPage";
import DashboardPage from "./pages/DashboardPage";

type View = "landing" | "dashboard";

function App() {
  const [view, setView] = useState<View>("landing");

  const handleNavigate = (newView: View) => {
    setView(newView);
  };

  if (view === "landing") {
    return <LandingPage onNavigate={() => handleNavigate("dashboard")} />;
  }

  return <DashboardPage onNavigate={handleNavigate} currentView={view} />;
}

export default App;
