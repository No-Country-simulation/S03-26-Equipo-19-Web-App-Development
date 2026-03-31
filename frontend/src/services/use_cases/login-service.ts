import type { LoginType } from "../../types/auth.types";
import { apiAuthService } from "../general_api";


export const postLogin = async (data: LoginType) => {
  try {
    const res = await apiAuthService.post("/login", data); 
     console.log('Inició sesión exitosamente');
    return res.data;
  } catch (error: any) {
  
    throw new Error(error.response?.data?.message || "Error de conexión");
  }
};