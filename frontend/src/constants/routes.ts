
export const ROUTES = {
  LOGIN: '/login',
  REGISTER: '/register',
  DASHBOARD: '/dashboard',

  CONTACTS: 'contacts',
  MESSAGES: 'messages',
  METRICS: 'metrics',
  SETTINGS: 'settings',
  TASKS: 'tasks',

  HOME: '/',
} as const;

export const ROUTE_BUILDERS = {
  userDetail: (userId: string) => `/dashboard/users/${userId}`,

} as const;

const API_BASE_URL =
  import.meta.env.VITE_URL_BASE ||
  import.meta.env.VITE_API_URL ||
  "http://localhost:3000";

const NORMALIZED_API_BASE = API_BASE_URL.replace(/\/+$/, "").replace(/\/api$/, "");

export const API_ENDPOINTS = {
  BASE: NORMALIZED_API_BASE,
  AUTH: {
    LOGIN: '/api/auth/login',
    REGISTER: '/api/auth/register',
  },
  USERS: {    // Agregar endpoints reales
    BASE: '/api/users',
    byId: (id: string | number) => `/api/users/${id}`,
  },
 
} as const;

