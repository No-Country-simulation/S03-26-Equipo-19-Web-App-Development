export const timeAgo = (dateString: string | Date) => {
  const now = new Date();
  const past = new Date(dateString);

  const diffMs = now.getTime() - past.getTime();

  const seconds = Math.floor(diffMs / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (seconds < 60) return "hace unos segundos";
  if (minutes < 60) return `hace ${minutes} min`;
  if (hours < 24) return `hace ${hours} h`;
  if (days < 7) return `hace ${days} días`;
  if (minutes === 1) return "hace 1 minuto";
  if (hours === 1) return "hace 1 hora";
  if (days === 1) return "ayer";

  // fallback → fecha normal
  return past.toLocaleDateString();
};
