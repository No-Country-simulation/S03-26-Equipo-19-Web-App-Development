import React from "react";
import { Sparkles, Globe, Shield } from "lucide-react";

interface FeaturesGridProps {}

const FeaturesGrid: React.FC<FeaturesGridProps> = () => {
  const features = [
    {
      icon: Sparkles,
      title: "IA Avanzada",
      desc: "Automatización inteligente con machine learning",
      color: "from-indigo-500 to-purple-500",
    },
    {
      icon: Globe,
      title: "Multi-Canal",
      desc: "WhatsApp, Email, SMS y más integrados",
      color: "from-blue-500 to-cyan-500",
    },
    {
      icon: Shield,
      title: "100% Seguro",
      desc: "Cifrado de extremo a extremo y GDPR",
      color: "from-emerald-500 to-teal-500",
    },
  ];

  return (
    <div className="mt-32 grid md:grid-cols-3 gap-6 max-w-4xl mx-auto">
      {features.map((feature, idx) => (
        <div
          key={idx}
          className="p-8 rounded-3xl bg-white/5 border border-white/10 backdrop-blur-sm hover:bg-white/10 hover:border-white/20 transition-all group cursor-pointer"
        >
          <div
            className={`w-14 h-14 rounded-2xl bg-gradient-to-r ${feature.color} flex items-center justify-center mb-5 shadow-lg group-hover:scale-110 transition-transform`}
          >
            <feature.icon size={28} className="text-white" />
          </div>
          <h3 className="text-xl font-bold text-white mb-2">{feature.title}</h3>
          <p className="text-slate-400">{feature.desc}</p>
        </div>
      ))}
    </div>
  );
};

export default FeaturesGrid;
