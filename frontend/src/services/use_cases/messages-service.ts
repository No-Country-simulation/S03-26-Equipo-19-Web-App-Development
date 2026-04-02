import {apiMessagesService } from "../general_api";

export const getMessages = async (token: string) => {

  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
console.log("URL:", apiMessagesService.defaults.baseURL);
  try {
    const res = await apiMessagesService.get("", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })

    return res.data;
  } catch (error: any) {
    throw new Error(error.response?.data?.message || "Error de conexión");
  }
};

