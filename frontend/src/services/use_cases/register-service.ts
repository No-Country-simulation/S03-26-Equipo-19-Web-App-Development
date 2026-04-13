import type { RegisterType } from "../../types/auth.types";
import { apiAuthService } from "../general_api";


export const postRegister = async (data: RegisterType) => {
  try {
    const res = await apiAuthService.post("/register", data);
    console.log('Se registró exitosamente');
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
  }
};
