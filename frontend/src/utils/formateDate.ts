export const formatDate = (dateStr: string | Date) => {
  const date = new Date(dateStr);
  return date.toLocaleDateString("es-AR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    timeZone: "UTC"
  });
};


export const formatDateTime = (dateStr: string | Date) => {
  const date = new Date(dateStr);

  return date.toLocaleString("es-AR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false, 
    timeZone: "UTC",
  });
};

export const formatTime = (iso: string) => {
  const date = new Date(iso);
  return date.toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' });
};
