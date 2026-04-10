import { useAuthStore } from "../../store/useAuthStore";
import type { MessageReqType } from "../../types/message.types";
import {apiMessagesService } from "../general_api";

//POSTS
export const postMessage = async (data: MessageReqType) => {
  const { token } = useAuthStore.getState();
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  console.log("Mensaje", {data});
  
  try {
    const res = await apiMessagesService.post("/send", data, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    console.log("Se envió el mensaje correctamente");
    return res.data;
  } catch (error: any) {
    throw new Error(error.response?.data?.message || "Error de conexión");
  }
};


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
