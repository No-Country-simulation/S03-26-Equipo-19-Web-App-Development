import { apiConversationsService } from "../general_api";
import type { ConversationResType } from "../../types/conversation.types";

export const getConversations = async (token: string): Promise<ConversationResType[]> => {
  if (!token) throw new Error("No hay token de autenticación.");
  try {
    const res = await apiConversationsService.get("", {
      headers: { Authorization: `Bearer ${token}` },
    });
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
  }
};

export const getConversationById = async (
  token: string,
  conversationId: number,
): Promise<ConversationResType> => {
  if (!token) throw new Error("No hay token de autenticación.");
  try {
    const res = await apiConversationsService.get(`/${conversationId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
  }
};

export const getConversationsByContactId = async (
  token: string,
  contactId: number,
): Promise<ConversationResType[]> => {
  if (!token) throw new Error("No hay token de autenticación.");
  try {
    const res = await apiConversationsService.get(`/contact/${contactId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
  }
};