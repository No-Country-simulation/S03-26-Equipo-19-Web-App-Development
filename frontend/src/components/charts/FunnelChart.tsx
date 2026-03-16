import React from "react";
import {
  Users,
  MessageSquare,
  Star,
  FileText,
  CheckCircle2,
  Target,
} from "lucide-react";

const FunnelChart: React.FC = () => {
  const stages = [
    {
      label: "Leads Totales",
      value: 100,
      color: "from-blue-500 to-blue-600",
      icon: Users,
    },
    {
      label: "Contactados",
      value: 75,
      color: "from-indigo-500 to-indigo-600",
      icon: MessageSquare,
    },
    {
      label: "Interesados",
      value: 45,
      color: "from-purple-500 to-purple-600",
      icon: Star,
    },
    {
      label: "Propuestas",
      value: 25,
      color: "from-amber-500 to-amber-600",
      icon: FileText,
    },
    {
      label: "Cerrados",
      value: 12,
      color: "from-emerald-500 to-emerald-600",
      icon: CheckCircle2,
    },
  ];

  return (
    <div className="bg-white p-6 rounded-[2rem] border border-slate-100 shadow-sm hover:shadow-lg transition-all duration-300">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-gradient-to-br from-amber-500 to-amber-600 rounded-xl shadow-lg shadow-amber-500/20">
            <Target size={18} className="text-white" />
          </div>
          <h3 className="font-bold text-slate-900">Funil de Conversión</h3>
        </div>
        <div className="text-xs font-bold text-slate-400">Pipeline ACTUAL</div>
      </div>
      <div className="space-y-3">
        {stages.map((stage, idx) => (
          <div key={idx} className="relative group/funnel">
            <div className="flex items-center justify-between mb-2">
              <div className="flex items-center gap-2">
                <span
                  className={`w-8 h-8 rounded-lg flex items-center justify-center bg-gradient-to-r ${stage.color} text-white text-xs font-bold shadow-lg`}
                >
                  {idx + 1}
                </span>
                <span className="text-sm font-semibold text-slate-700">
                  {stage.label}
                </span>
              </div>
              <span className="text-sm font-black text-slate-900">
                {stage.value}%
              </span>
            </div>
            <div className="h-4 bg-slate-100 rounded-full overflow-hidden">
              <div
                className={`h-full bg-gradient-to-r ${stage.color} rounded-full transition-all duration-1000 shadow-inner`}
                style={{ width: `${stage.value}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default FunnelChart;
