import React from "react";
import { Zap, Rocket, ArrowRight, Play } from "lucide-react";

interface HeroSectionProps {
  onNavigate: () => void;
}

const HeroSection: React.FC<HeroSectionProps> = ({ onNavigate }) => {
  return (
    <div className="min-h-screen bg-[#0a0a0f] text-white font-sans overflow-hidden relative selection:bg-indigo-500/30">
      {/* Animated Background */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-0 -left-20 w-[500px] h-[500px] bg-indigo-600/30 rounded-full blur-[150px]" />
        <div className="absolute bottom-0 -right-20 w-[500px] h-[500px] bg-purple-600/30 rounded-full blur-[150px]" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[1000px] h-[1000px] bg-gradient-to-br from-indigo-500/5 via-purple-500/5 to-transparent rounded-full blur-[120px]" />

        {/* Grid Pattern */}
        <div className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.02)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.02)_1px,transparent_1px)] bg-[size:60px_60px]" />
      </div>

      {/* Navigation */}
      <nav className="fixed w-full z-50 bg-black/20 backdrop-blur-2xl border-b border-white/10 py-5">
        <div className="max-w-7xl mx-auto px-8 flex justify-between items-center">
          <div
            className="flex items-center gap-3 font-black text-2xl tracking-tight cursor-pointer"
            onClick={onNavigate}
          >
            <div className="bg-gradient-to-br from-indigo-500 via-purple-500 to-indigo-500 p-2 rounded-xl text-white shadow-lg shadow-indigo-500/30 hover:scale-110 transition-transform">
              <Zap size={24} fill="currentColor" />
            </div>
            <span className="bg-clip-text text-transparent bg-gradient-to-r from-white via-slate-200 to-slate-400">
              Nexus
            </span>
            <span className="text-xs font-bold bg-gradient-to-r from-indigo-400 to-purple-400 text-transparent bg-clip-text px-2 py-0.5 rounded-full border border-indigo-500/30">
              PRO
            </span>
          </div>
          <div className="flex items-center gap-4">
            <button className="text-sm font-medium text-slate-300 hover:text-white transition-colors">
              Características
            </button>
            <button className="text-sm font-medium text-slate-300 hover:text-white transition-colors">
              Precios
            </button>
            <button
              onClick={onNavigate}
              className="bg-white/10 backdrop-blur-sm border border-white/20 text-white px-5 py-2.5 rounded-full font-bold text-sm hover:bg-white/20 hover:scale-105 transition-all"
            >
              Abrir App
            </button>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="pt-56 pb-40 px-6 text-center max-w-5xl mx-auto relative z-10">
        <div className="inline-flex items-center gap-3 px-5 py-2.5 rounded-full bg-white/5 border border-white/10 text-sm font-medium mb-10 animate-fade-in-up">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse shadow-lg shadow-emerald-400/50" />
          <span className="text-slate-300">
            🚀 La nueva generación de CRM con IA
          </span>
          <ArrowRight size={16} className="text-indigo-400" />
        </div>

        <h1
          className="text-6xl md:text-7xl lg:text-8xl font-black mb-10 leading-[1.02] tracking-tight animate-fade-in-up"
          style={{ animationDelay: "0.1s" }}
        >
          Vende más rápido con <br />
          <span className="bg-clip-text text-transparent bg-gradient-to-r from-indigo-400 via-purple-400 to-indigo-400 bg-[length:200%_auto] animate-gradient">
            inteligencia real
          </span>
        </h1>

        <p
          className="text-xl md:text-2xl text-slate-400 mb-12 leading-relaxed max-w-2xl mx-auto animate-fade-in-up"
          style={{ animationDelay: "0.2s" }}
        >
          El CRM más avanzado para startups que quieren escalar. WhatsApp, Email
          y automatización impulsada por IA en una sola plataforma.
        </p>

        <div
          className="flex flex-col sm:flex-row gap-5 justify-center items-center animate-fade-in-up"
          style={{ animationDelay: "0.3s" }}
        >
          <button
            onClick={onNavigate}
            className="group bg-gradient-to-r from-indigo-500 via-purple-500 to-indigo-500 text-white px-10 py-5 rounded-2xl font-bold text-lg shadow-2xl shadow-indigo-500/25 hover:shadow-indigo-500/40 hover:scale-105 transition-all flex items-center gap-3"
          >
            <Rocket size={22} />
            Comenzar Gratis
            <ArrowRight
              size={18}
              className="group-hover:translate-x-1 transition-transform"
            />
          </button>
          <button className="group bg-white/5 border border-white/10 px-10 py-5 rounded-2xl font-bold text-lg hover:bg-white/10 transition-all backdrop-blur-sm flex items-center gap-3">
            <Play size={18} className="fill-white" />
            Ver Demo
          </button>
        </div>

        {/* Floating Cards Animation */}
        <div className="mt-20 relative">
          <div className="absolute -top-20 -left-20 w-64 h-64 bg-indigo-500/20 rounded-full blur-3xl animate-pulse" />
          <div
            className="absolute -bottom-20 -right-20 w-64 h-64 bg-purple-500/20 rounded-full blur-3xl animate-pulse"
            style={{ animationDelay: "1s" }}
          />
        </div>
      </section>
    </div>
  );
};

export default HeroSection;
