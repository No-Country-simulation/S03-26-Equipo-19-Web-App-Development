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
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
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
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
  }
};
