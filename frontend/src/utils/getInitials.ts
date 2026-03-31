export const getInitials = (name?: string) => {
  if (!name) return "U";

  const parts = name.trim().split(" ");

  if (parts.length === 1) {
    return parts[0].charAt(0);
  }

  const first = parts[0].charAt(0);
  const last = parts[parts.length - 1].charAt(0);

  return `${first}${last}`;
};