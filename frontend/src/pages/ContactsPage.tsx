import TitleSection from '../components/ui/TitleSection'
import { Button } from '../components/ui/Button'
import { ContactsTable } from '../components/dashboard/ContactsTable';

export const ContactsPage = () => {

  

  return (
    <>
      <div className="flex justify-center md:justify-between mb-6">
        <TitleSection text="Mis contactos" className='hidden md:flex' />
        <Button variant='secondary' onClick={() => {}}> //conectar modal para crear nuevo contacto
          Nuevo contacto
        </Button>
      </div>

      <div className=""><ContactsTable /></div>

    </>
  )
}


