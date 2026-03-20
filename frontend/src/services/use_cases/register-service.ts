import type { RegisterType } from "../../types/auth.types";
import { apiAuthService } from "../general_api";


export const postRegister = async (data: RegisterType) => {
  try {
    const res = await apiAuthService.post("/register", data);
    console.log('Se registró exitosamente');
    return res.data;
  } catch (error: any) {
  
    throw new Error(error.response?.data?.message || "Error de conexión");
  }
};
