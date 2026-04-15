export const dateAdapter = {
  toInput: (value: string) => {
    if (!value) return "";
    return value.slice(0, 16);
  },

  toBackend: (value: string) => {
    if (!value) return value;
    return value.length === 16 ? `${value}:00` : value;
  },
};