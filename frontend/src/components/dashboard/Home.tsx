import { Button } from '../ui/Button'
import { KpiCard } from '../ui/KpiCard'
import { ContactsTable } from './ContactsTable'
import { Calendar, CalendarCheck, CalendarX, UserStar } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { ROUTES } from '../../constants/routes'
import TitleSection from '../ui/TitleSection'


const Home = () => {

  const navigate = useNavigate();

  return (
    <>
      <div className="flex justify-center md:justify-between mb-6">
        <TitleSection text="Dashboard" className='hidden md:flex'/>
        <Button variant='secondary' className="w-1/2 md:w-1/4 lg:w-1/6" onClick={() => navigate(`/dashboard/${ROUTES.TASKS}`)}>
          Ver Tareas
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
        <KpiCard title="Tareas para hoy" value="15" trend="8.5%" color="primary" isPositive={true} icon={<CalendarCheck size={30} />} />
        <KpiCard title="Tareas vencidas" value="09" trend="2.1%" color="error" isPositive={false} icon={<CalendarX size={28}/>} />
        <KpiCard title="Próximas tareas" value="125" trend="4.3%" color="secondary" isPositive={true} icon={<Calendar size={28}  />} />
        <KpiCard title="Contactos nuevos" value="23" trend="4.3%" color="success" isPositive={true} icon={<UserStar size={28}/>} />
      </div>

<div className="flex justify-center md:justify-end mb-6">
        <Button variant='primary' className="w-1/2 md:w-1/4 lg:w-1/6" onClick={() => navigate(`/dashboard/${ROUTES.CONTACTS}`)}>
          Ver Contactos
        </Button>
      </div>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <div className="lg:col-span-1"><ContactsTable /></div>
      </div>
    </>
  )
}

export default Home
