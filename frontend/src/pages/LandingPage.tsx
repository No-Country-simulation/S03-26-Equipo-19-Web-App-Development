import React from "react";
import HeroSection from "../components/landing/HeroSection";
import StatsPreview from "../components/landing/StatsPreview";
import FeaturesGrid from "../components/landing/FeaturesGrid";

interface LandingPageProps {
  onNavigate: () => void;
}

const LandingPage: React.FC<LandingPageProps> = ({ onNavigate }) => {
  return (
    <div className="min-h-screen bg-[#0a0a0f] text-white font-sans overflow-hidden relative selection:bg-indigo-500/30">
      <HeroSection onNavigate={onNavigate} />
      <StatsPreview />
      <FeaturesGrid />

      {/* Bottom spacing */}
      <div className="h-32" />
    </div>
  );
};

export default LandingPage;
