import type { LoginType } from "../../types/auth.types";
import { apiAuthService } from "../general_api";

export const postLogin = async (data: LoginType) => {
  try {
    const res = await apiAuthService.post("/login", data);
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
  }
};