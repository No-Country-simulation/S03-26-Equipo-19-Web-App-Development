import {apiMessagesService } from "../general_api";

export const getMessages = async (token: string) => {

  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
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


// Mensajes por id Conversación
export const getMessagesByConversationId = async (token: string, conversationId: number) => {
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiMessagesService.get(`/conversations/${conversationId}/history`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  } catch (error: any) {
    throw new Error(error.response?.data?.message || "Error de conexión");
  }
};
