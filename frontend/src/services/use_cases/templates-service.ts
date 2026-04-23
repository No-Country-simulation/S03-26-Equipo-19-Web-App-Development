import { apiTemplatesService } from "../general_api";

export const getTemplates = async (token: string) => {
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiTemplatesService.get("", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
  }
};