import { User, Trash2 } from 'lucide-react';

// Datos de prueba (mock data) basados en la imagen
const contactsData = [
  {
    id: 1,
    name: 'Jorge R. Konner',
    company: 'Companies, Inc.',
    status: '#1E3A8A',
    lastActive: '2 days ago',
    badgeColor: 'bg-[#13316b] text-white',
  },
  {
    id: 2,
    name: 'Sara M. Righn',
    company: 'Company',
    status: '#38BDF8',
    lastActive: '2 min ago',
    badgeColor: 'bg-[#38bdf8] text-white',
  },
  {
    id: 3,
    name: 'Alex P. Borman',
    company: 'CoreCRM',
    status: 'Awaiting',
    lastActive: '2 May 2023',
    badgeColor: 'bg-slate-100 text-slate-500', // Estado neutral
  },
];

export const ContactsTable = () => {
  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm h-full flex flex-col">
      <h3 className="text-sm font-bold text-slate-700 mb-4">Contactos Recientes</h3>
      
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-slate-100 text-sm font-semibold text-slate-800">
              <th className="py-3 px-2">Name</th>
              <th className="py-3 px-2">Company</th>
              <th className="py-3 px-2">Status</th>
              <th className="py-3 px-2">Last Active</th>
              <th className="py-3 px-2 text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {contactsData.map((contact) => (
              <tr key={contact.id} className="border-b border-slate-50 last:border-0 hover:bg-slate-50 transition-colors text-sm text-slate-600">
                <td className="py-4 px-2 font-medium text-slate-800">{contact.name}</td>
                <td className="py-4 px-2">{contact.company}</td>
                <td className="py-4 px-2">
                  <span className={`px-3 py-1 rounded-md text-xs font-medium ${contact.badgeColor}`}>
                    {contact.status}
                  </span>
                </td>
                <td className="py-4 px-2">{contact.lastActive}</td>
                <td className="py-4 px-2 flex justify-end gap-3">
                  {/* Botón de Perfil con accesibilidad */}
                  <button 
                    aria-label={`Ver perfil de ${contact.name}`}
                    title="Ver perfil"
                    className={`p-1.5 rounded-md ${contact.status.startsWith('#') ? contact.badgeColor : 'bg-slate-100 text-slate-400 hover:text-slate-600'}`}
                  >
                    <User size={16} />
                  </button>
                  {/* Botón de Eliminar con accesibilidad */}
                  <button 
                    aria-label={`Eliminar a ${contact.name}`}
                    title="Eliminar contacto"
                    className="p-1.5 rounded-md text-slate-400 hover:text-rose-500 hover:bg-rose-50 transition-colors"
                  >
                    <Trash2 size={16} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};