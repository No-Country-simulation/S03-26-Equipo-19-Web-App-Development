export const getLast30DaysRange = () => {
  const endDate = new Date();
  const startDate = new Date();

  startDate.setDate(endDate.getDate() - 30);

  return {
    startDate: startDate.toISOString().split("T")[0],
    endDate: endDate.toISOString().split("T")[0],
  };
};