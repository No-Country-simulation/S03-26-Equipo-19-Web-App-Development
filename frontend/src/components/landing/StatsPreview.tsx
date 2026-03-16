import React from "react";

interface StatsPreviewProps {}

const StatsPreview: React.FC<StatsPreviewProps> = () => {
  const stats = [
    {
      number: "10K+",
      label: "Leads gestionados",
      color: "from-blue-500 to-blue-600",
    },
    {
      number: "340%",
      label: "ROI promedio",
      color: "from-purple-500 to-purple-600",
    },
    {
      number: "24/7",
      label: "Automatización",
      color: "from-emerald-500 to-emerald-600",
    },
    { number: "99.9%", label: "Uptime", color: "from-amber-500 to-amber-600" },
  ];

  return (
    <div
      className="mt-24 grid grid-cols-2 md:grid-cols-4 gap-4 max-w-4xl mx-auto animate-fade-in-up"
      style={{ animationDelay: "0.4s" }}
    >
      {stats.map((stat, idx) => (
        <div
          key={idx}
          className="p-6 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-sm hover:bg-white/10 hover:border-white/20 transition-all group"
        >
          <div
            className={`text-4xl font-black bg-gradient-to-r ${stat.color} bg-clip-text text-transparent mb-1 group-hover:scale-110 transition-transform inline-block`}
          >
            {stat.number}
          </div>
          <div className="text-sm text-slate-400">{stat.label}</div>
        </div>
      ))}
    </div>
  );
};

export default StatsPreview;
