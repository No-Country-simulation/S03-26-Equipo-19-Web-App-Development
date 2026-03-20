import React from 'react'
import { Button } from '../ui/Button'
import { KpiCard } from './KpiCard'
import { MessagesChart } from './MessagesChart'
import { GrowthChart } from './GrowthChart'
import { ChannelsChart } from './ChannelsChart'
import { ContactsTable } from './ContactsTable'
import { AlertsList } from './AlertsList'
import { Mail, MessageSquare, Users } from 'lucide-react'

const Home = () => {
  return (
    <>
    <div className="flex justify-end mb-6">
        <Button className="bg-sky-500 hover:bg-sky-600 text-white px-5 py-2.5 rounded-xl font-semibold text-sm ">
          Add New Contact
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <KpiCard title="Contactos Activos" value="1,452" trend="8.5%" isPositive={true} icon={<Users size={28} strokeWidth={2.5} />} />
        <KpiCard title="Mensajes Enviados" value="31,890" trend="2.1%" isPositive={false} icon={<Mail size={28} strokeWidth={2.5} />} />
        <KpiCard title="Tasa de Respuesta" value="28.4%" trend="4.3%" isPositive={true} icon={<MessageSquare size={28} strokeWidth={2.5} />} />
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
      </>
  )
}

export default Home
