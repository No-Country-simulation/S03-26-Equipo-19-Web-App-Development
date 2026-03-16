import React from "react";
import { Plus } from "lucide-react";
import { Button } from "../components/ui/Button";
import { useLeads } from "../hooks/useLeads";
import { useModal } from "../hooks/useModal";
import { LeadFormData } from "../types";
import Layout from "../components/layout/Layout";
import KPIGrid from "../components/dashboard/KPIGrid";
import AnalyticsSection from "../components/dashboard/AnalyticsSection";
import LeadsTable from "../components/dashboard/LeadsTable";
import LeadModal from "../components/dashboard/LeadModal";

interface DashboardPageProps {
  onNavigate: (view: "landing" | "dashboard") => void;
  currentView: "landing" | "dashboard";
}

const DashboardPage: React.FC<DashboardPageProps> = ({
  onNavigate,
  currentView,
}) => {
  const { leads, loading, stats, addLead, deleteLead } = useLeads();
  const { isOpen, open, close } = useModal();

  const handleCreateLead = (data: LeadFormData) => {
    addLead(data);
  };

  return (
    <Layout
      onNavigate={onNavigate}
      currentView={currentView}
      onOpenModal={open}
      showSidebar={true}
    >
      <div className="flex-1 overflow-y-auto p-6 md:p-10 bg-slate-50/30">
        {/* Header Section */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-10">
          <div className="relative">
            <div className="absolute -top-2 -left-2 w-16 h-16 bg-indigo-500/10 rounded-full blur-xl" />
            <div className="absolute -bottom-2 -right-2 w-12 h-12 bg-purple-500/10 rounded-full blur-xl" />
            <div className="relative">
              <h1 className="text-4xl font-black text-slate-900 mb-2">
                Buenos días, Equipo 🚀
              </h1>
              <p className="text-slate-500 text-base font-medium">
                Aquí tienes el resumen de tu funnel de ventas hoy.
              </p>
            </div>
          </div>
          <Button onClick={open}>
            <Plus size={22} /> Nuevo Lead
          </Button>
        </div>

        {/* KPI Grid */}
        <KPIGrid stats={stats} />

        {/* Analytics Section */}
        <AnalyticsSection />

        {/* Leads Table */}
        <LeadsTable
          leads={leads}
          loading={loading}
          totalLeads={stats.total}
          onDelete={deleteLead}
        />
      </div>

      {/* Lead Modal */}
      <LeadModal isOpen={isOpen} onClose={close} onSubmit={handleCreateLead} />
    </Layout>
  );
};

export default DashboardPage;
