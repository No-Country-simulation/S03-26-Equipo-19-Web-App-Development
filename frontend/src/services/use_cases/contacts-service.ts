import { useAuthStore } from "../../store/useAuthStore";
import type { ContactReqType } from "../../types/contact.types";
import { apiContactsService } from "../general_api";

//POSTS
export const postContact = async (data: ContactReqType) => {
  const { token } = useAuthStore.getState();
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiContactsService.post("", data, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    console.log("Se registró exitosamente el contacto");
    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
  }
};

// Todos los contactos (ya filtra por token de usuario: admin / vendedores)
export const getContacts = async (token: string) => {
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiContactsService.get("", {
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

//  lista de contactos del usuario autenticado con metricas, msjs no leidos 
export const getContactsDashboard = async (token: string) => {
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiContactsService.get("/dashboard", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
console.log("Data dashboard", res);

    return res.data;
  } catch (error: any) {
    throw new Error(error.response?.data?.message || "Error de conexión");
  }
};


// Contacto por ID
export const getContactById = async (token: string, contactId: number) => {
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiContactsService.get(`/${contactId}`, {
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





//Actualizar contacto por ID
export const updateContactById = async (contactId: number, data: ContactReqType) => {
  const { token } = useAuthStore.getState();
  if (!token) {
    throw new Error("No hay token de autenticación.");
  }
  try {
    const res = await apiContactsService.put(`/${contactId}`, data, {
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


// Actualizar funnel-status
export const updateFunnelStatusByContactId = async ( contactId: number, status: string) => {
  const { token } = useAuthStore.getState();

  if (!token) {
    throw new Error("No hay token de autenticación.");
  }

  try {
    const res = await apiContactsService.patch(
      `/${contactId}/funnel-status`,
      null, 
      {
        params: { status }, 
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    return res.data;
  } catch (error: unknown) {
    const msg = error instanceof Error ? error.message : "Error de conexión";
    throw new Error((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? msg);
  }
};



