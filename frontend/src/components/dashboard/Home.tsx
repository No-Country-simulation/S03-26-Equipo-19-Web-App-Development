import { Button } from '../ui/Button'
import { KpiCard } from '../ui/KpiCard'
import { ContactsTable } from './ContactsTable'
import { Calendar, CalendarCheck, CalendarX, UserStar } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { ROUTES } from '../../constants/routes'
import TitleSection from '../ui/TitleSection'
import { InboxTable } from './InboxTable'
import { useGetContacts } from '../../services/use_queries/contacts-query'
import { useGetContactsMetrics, useGetTasksMetrics } from '../../services/use_queries/metrics-query'
import { useAuthStore } from '../../store/useAuthStore'



const Home = () => {

  const navigate = useNavigate();
  const {user} = useAuthStore()

  const { data: tasksMetrics } = useGetTasksMetrics()
  const { data: contactsMetrics } = useGetContactsMetrics()

  const { data: contacts, isLoading: isLoadingContacts } = useGetContacts()

  return (
    <>
      <div className="flex items-center justify-center md:justify-between mb-6">
        <div>
          <TitleSection text="Dashboard" className='hidden md:flex' />
          <p className="text-slate-500 text-sm mt-0.5">
            Bienvenido de nuevo, {user?.name ?? 'Vendedor'}. Esto es lo que está sucediendo hoy.
          </p>
        </div>
        <Button variant='secondary' className="w-1/2 md:w-1/4 lg:w-1/6 h-[40px]" onClick={() => navigate(`/dashboard/${ROUTES.TASKS}`)}>
          Ver Tareas
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
        <KpiCard
          title="Tareas para hoy"
          metric={tasksMetrics?.tasks?.dueToday ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="primary"
          icon={<CalendarCheck size={28} />}
        />
        <KpiCard
          title="Tareas vencidas"
          metric={tasksMetrics?.tasks?.overdue ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="error"
          icon={<CalendarX size={28} />}
          impact="negative"
        />
        <KpiCard
          title="Próximas tareas"
          metric={tasksMetrics?.tasks?.pending ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="secondary"
          icon={<Calendar size={28} />}
        />
        <KpiCard
          title="Nuevos contactos"
          metric={contactsMetrics?.funnel.byStatus.NEW_LEAD ?? { value: 0, changePercent: 0, trend: 'stable' }}
          color="success"
          icon={<UserStar size={28} />}
        />
      </div>

      <div className="flex justify-center md:justify-end mb-6">
        <Button variant='primary' className="w-1/2 md:w-1/4 lg:w-1/6" onClick={() => navigate(`/dashboard/${ROUTES.CONTACTS}`)}>
          Ver Contactos
        </Button>
      </div>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <div className="lg:col-span-1"><InboxTable /></div>
        <div className="lg:col-span-1"><ContactsTable contacts={contacts ?? []} isLoading={isLoadingContacts} /></div>
      </div>
    </>
  )
}

export default Home
