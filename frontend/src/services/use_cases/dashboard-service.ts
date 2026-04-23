import { apiContactsService } from "../general_api";


export const getContactsDashboard = async () => {
  const res = await apiContactsService.get("/dashboard");
  return res.data;
};

