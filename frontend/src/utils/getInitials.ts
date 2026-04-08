export const getInitials = (name?: string, lastName?: string) => {
  if (!name) return "U";

  // 👉 Caso 2 parámetros
if (lastName?.trim()) {
    return `${name.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
  }

  // 👉 Caso 1 string (nombre completo)
  const parts = name.trim().split(" ");

  if (parts.length === 1) {
    return parts[0].charAt(0).toUpperCase();
  }

  const first = parts[0].charAt(0);
  const last = parts[parts.length - 1].charAt(0);

  return `${first}${last}`.toUpperCase();
};