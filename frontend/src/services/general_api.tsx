import axios, { type InternalAxiosRequestConfig } from "axios";
import { useAuthStore } from "../store/useAuthStore";

// --- Interceptor JWT reutilizable ---
const addAuthInterceptor = (instance: ReturnType<typeof axios.create>) => {
  instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });
  return instance;
};

export const apiAuthService = addAuthInterceptor(
  axios.create({ baseURL: `${import.meta.env.VITE_URL_BASE}/auth` })
);

export const apiContactsService = addAuthInterceptor(
  axios.create({ baseURL: `${import.meta.env.VITE_URL_BASE}/contacts` })
);

export const apiMessagesService = addAuthInterceptor(
  axios.create({ baseURL: `${import.meta.env.VITE_URL_BASE}/messages` })
);

export const apiSalespersonService = addAuthInterceptor(
  axios.create({ baseURL: `${import.meta.env.VITE_URL_BASE}/salespersons` })
);

export const apiTemplatesService = addAuthInterceptor(
  axios.create({ baseURL: `${import.meta.env.VITE_URL_BASE}/templates` })
);

export const apiConversationsService = addAuthInterceptor(
  axios.create({ baseURL: `${import.meta.env.VITE_URL_BASE}/conversations` })
);

export const apiWebhooksService = addAuthInterceptor(
  axios.create({ baseURL: `${import.meta.env.VITE_URL_BASE}/webhooks` })
);

export const apiTagsService = addAuthInterceptor(
  axios.create({ baseURL: `${import.meta.env.VITE_URL_BASE}/tags` })
);

export const apiMetricsService = addAuthInterceptor(
  axios.create({ baseURL: `${import.meta.env.VITE_URL_BASE}/metrics` })
);

export const apiExportService = addAuthInterceptor(
  axios.create({ baseURL: `${import.meta.env.VITE_URL_BASE}/export` })
);

export const apiSavedViewsService = addAuthInterceptor(
  axios.create({ baseURL: `${import.meta.env.VITE_URL_BASE}/saved-views` })
);

export const apiTasksService = addAuthInterceptor(
  axios.create({ baseURL: `${import.meta.env.VITE_URL_BASE}/tasks` })
);