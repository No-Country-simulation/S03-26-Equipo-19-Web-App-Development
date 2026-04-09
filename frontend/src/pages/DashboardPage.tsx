import { KpiCard } from '../components/ui/KpiCard';
import { MessagesChart } from '../components/dashboard/MessagesChart';
import { GrowthChart } from '../components/dashboard/GrowthChart';
import { ChannelsChart } from '../components/dashboard/ChannelsChart';
import { ContactsTable } from '../components/dashboard/ContactsTable';
import { AlertsList } from '../components/dashboard/AlertsList';
import { Users, Mail, MessageSquare } from 'lucide-react';

export const DashboardPage = () => {
  return (
    <div>
      <div className="flex justify-end mb-6">
        <button className="bg-sky-500 hover:bg-sky-600 text-white px-5 py-2.5 rounded-xl font-semibold text-sm transition-colors shadow-sm">
          Add New Contact
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <KpiCard 
  title="Contactos Activos"
  value="1,452"
  trend="8.5%"
  isPositive={true}
  color="primary"
  icon={<Users size={28} strokeWidth={2.5} />}
/>
       <KpiCard 
  title="Mensajes Enviados"
  value="31,890"
  trend="2.1%"
  isPositive={false}
  color="secondary"
  icon={<Mail size={28} strokeWidth={2.5} />}
/>

<KpiCard 
  title="Tasa de Respuesta"
  value="28.4%"
  trend="4.3%"
  isPositive={true}
  color="success"
  icon={<MessageSquare size={28} strokeWidth={2.5} />}
/>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        <div className="lg:col-span-1"><MessagesChart /></div>
        <div className="lg:col-span-1"><GrowthChart /></div>
        <div className="lg:col-span-1"><ChannelsChart /></div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <div className="lg:col-span-2"><ContactsTable /></div>
        <div className="lg:col-span-1"><AlertsList /></div>
      </div>
    </div>
  );
};