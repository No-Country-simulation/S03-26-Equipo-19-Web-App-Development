import React from "react";
import {
  Activity,
  Target,
  Clock,
  Database,
  Star,
  CheckCircle2,
  TrendingUp,
} from "lucide-react";
import { BarChart } from "../charts/BarChart";
import { LineChart } from "../charts/LineChart";
import { PieChart } from "../charts/PieChart";
import FunnelChart from "../charts/FunnelChart";

type CardColor = "blue" | "emerald" | "amber" | "purple";

const AnalyticsSection: React.FC = () => {
  const statCards: {
    title: string;
    value: string;
    change: string;
    trend: "up" | "down";
    icon: typeof Target;
    color: CardColor;
  }[] = [
    {
      title: "Tasa de Conversión",
      value: "24.5%",
      change: "+3.2%",
      trend: "up",
      icon: Target,
      color: "purple",
    },
    {
      title: "Tiempo Promedio",
      value: "4.2d",
      change: "-1.5%",
      trend: "up",
      icon: Clock,
      color: "blue",
    },
    {
      title: "Valor Promedio",
      value: "$1,240",
      change: "+8.7%",
      trend: "up",
      icon: Database,
      color: "emerald",
    },
    {
      title: "Satisfacción",
      value: "4.8/5",
      change: "+0.2%",
      trend: "up",
      icon: Activity,
      color: "amber",
    },
  ];

  const colorClasses: Record<CardColor, string> = {
    blue: "bg-gradient-to-br from-blue-50 to-blue-100 text-blue-600",
    emerald:
      "bg-gradient-to-br from-emerald-50 to-emerald-100 text-emerald-600",
    amber: "bg-gradient-to-br from-amber-50 to-amber-100 text-amber-600",
    purple: "bg-gradient-to-br from-purple-50 to-purple-100 text-purple-600",
  };

  const advancedMetrics = [
    {
      label: "Retención de Clientes",
      value: "87.5%",
      color: "emerald",
      icon: CheckCircle2,
    },
    { label: "Costo por Lead", value: "$12.40", color: "blue", icon: Database },
    {
      label: "ROI Marketing",
      value: "342%",
      color: "purple",
      icon: TrendingUp,
    },
    { label: "NPS Score", value: "72", color: "amber", icon: Star },
  ];

  return (
    <div className="mb-10">
      <div className="flex items-center justify-between mb-8">
        <div className="flex items-center gap-4">
          <div className="p-3 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl shadow-lg shadow-indigo-500/30">
            <Activity size={22} className="text-white" />
          </div>
          <div>
            <h2 className="text-2xl font-black text-slate-900">Análisis SAS</h2>
            <p className="text-sm text-slate-400 font-medium">
              Métricas avanzadas en tiempo real
            </p>
          </div>
        </div>
        <div className="flex gap-3">
          <button className="px-4 py-2 rounded-xl bg-white border border-slate-200 text-sm font-semibold text-slate-600 hover:bg-slate-50 transition-all">
            Este Mes
          </button>
          <button className="px-4 py-2 rounded-xl bg-indigo-50 text-indigo-600 text-sm font-bold hover:bg-indigo-100 transition-all">
            Ver Todo
          </button>
        </div>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {statCards.map((card, idx) => (
          <div
            key={idx}
            className="bg-white p-5 rounded-[2rem] border border-slate-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 group"
          >
            <div className="flex items-start justify-between mb-4">
              <div
                className={`p-3 rounded-2xl ${colorClasses[card.color]} group-hover:scale-110 transition-transform shadow-md`}
              >
                <card.icon size={22} />
              </div>
              <div
                className={`flex items-center gap-1 text-xs font-bold px-3 py-1.5 rounded-full ${card.trend === "up" ? "bg-emerald-50 text-emerald-600" : "bg-red-50 text-red-600"}`}
              >
                {card.trend === "up" ? (
                  <TrendingUp size={14} />
                ) : (
                  <Clock size={14} />
                )}
                {card.change}
              </div>
            </div>
            <div className="text-3xl font-black text-slate-900 mb-1 group-hover:text-indigo-600 transition-colors">
              {card.value}
            </div>
            <div className="text-xs font-semibold text-slate-400">
              {card.title}
            </div>
          </div>
        ))}
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <BarChart
          title="Leads por Canal"
          data={[
            { label: "WhatsApp", value: 65 },
            { label: "Email", value: 45 },
            { label: "Web", value: 35 },
            { label: "Referido", value: 25 },
            { label: "Ads", value: 20 },
          ]}
        />
        <LineChart
          title="Tendencia de Ventas"
          data={[
            { value: 20 },
            { value: 35 },
            { value: 30 },
            { value: 50 },
            { value: 45 },
            { value: 70 },
            { value: 65 },
            { value: 80 },
          ]}
        />
        <PieChart
          title="Distribución por Estado"
          data={[
            { label: "Lead Activo", value: 45, percent: 45 },
            { label: "En Seguimiento", value: 30, percent: 30 },
            { label: "Cerrado", value: 25, percent: 25 },
          ]}
          colors={["#6366f1", "#f59e0b", "#10b981"]}
        />
      </div>

      {/* Conversion Funnel & Advanced Metrics */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <FunnelChart />
        <div className="bg-white p-7 rounded-[2rem] border border-slate-100 shadow-sm hover:shadow-lg transition-all duration-300">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-3">
              <div className="p-2.5 bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl shadow-lg shadow-blue-500/20">
                <TrendingUp size={20} className="text-white" />
              </div>
              <h3 className="font-bold text-slate-900 text-lg">
                Métricas Avanzadas SAS
              </h3>
            </div>
          </div>
          <div className="space-y-4">
            {advancedMetrics.map((metric, idx) => (
              <div
                key={idx}
                className="flex items-center justify-between p-4 bg-slate-50/50 rounded-2xl hover:bg-slate-100 transition-all group cursor-pointer"
              >
                <div className="flex items-center gap-3">
                  <div
                    className={`w-10 h-10 rounded-xl flex items-center justify-center ${colorClasses[metric.color as CardColor]} group-hover:scale-110 transition-transform`}
                  >
                    <metric.icon size={18} />
                  </div>
                  <span className="text-sm font-semibold text-slate-700">
                    {metric.label}
                  </span>
                </div>
                <span className="text-lg font-black text-slate-900">
                  {metric.value}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AnalyticsSection;
