import { apiConversationsService } from "../general_api";
import type {
  ConversationInboxItemType,
  ConversationResType,
} from "../../types/conversation.types";
import { useAuthStore } from "../../store/useAuthStore";

export const getConversations = async (
  token: string,
): Promise<ConversationResType[]> => {
  if (!token) throw new Error("No hay token de autenticación.");
  try {
    const res = await apiConversationsService.get("", {
      headers: { Authorization: `Bearer ${token}` },
    });
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error(
      (error as { response?: { data?: { message?: string } } }).response?.data
        ?.message ?? msg,
    );
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
    throw new Error(
      (error as { response?: { data?: { message?: string } } }).response?.data
        ?.message ?? msg,
    );
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
    throw new Error(
      (error as { response?: { data?: { message?: string } } }).response?.data
        ?.message ?? msg,
    );
  }
};

export const getConversationsInbox = async (
  token: string,
): Promise<ConversationInboxItemType[]> => {
  if (!token) throw new Error("No hay token de autenticación.");
  try {
    const res = await apiConversationsService.get("/inbox", {
      headers: { Authorization: `Bearer ${token}` },
    });
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error(
      (error as { response?: { data?: { message?: string } } }).response?.data
        ?.message ?? msg,
    );
  }
};

export const closeConversationById = async (
  conversationId: number,
): Promise<ConversationResType> => {
  const { token } = useAuthStore.getState();
  if (!token) throw new Error("No hay token de autenticación.");
  try {
    const res = await apiConversationsService.patch(
      `/${conversationId}/close`,
      null,
      {
        headers: { Authorization: `Bearer ${token}` },
      },
    );
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error(
      (error as { response?: { data?: { message?: string } } }).response?.data
        ?.message ?? msg,
    );
  }
};

export const reOpenConversationById = async (
  conversationId: number,
): Promise<ConversationResType> => {
  const { token } = useAuthStore.getState();
  if (!token) throw new Error("No hay token de autenticación.");
  try {
     const res = await apiConversationsService.patch(
      `/${conversationId}/reopen`,
      null,
      {
        headers: { Authorization: `Bearer ${token}` },
      },
    );
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error(
      (error as { response?: { data?: { message?: string } } }).response?.data
        ?.message ?? msg,
    );
  }
};

