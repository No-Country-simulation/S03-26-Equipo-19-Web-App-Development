export const getStatusLabel = (status: string) => {
  switch (status) {
    case 'NEW_LEAD': return 'Nuevo';
    case 'CONTACTED': return 'Contactado';
    case 'IN_NEGOTIATION': return 'En negociación';
    case 'PROPOSAL_SENT': return 'Propuesta enviada';
    case 'CLOSED_WON': return 'Ganado';
    case 'CLOSED_LOST': return 'Perdido';
    default: return status;
  }
};