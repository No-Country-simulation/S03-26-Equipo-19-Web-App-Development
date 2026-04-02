export const ROUTES = {
  LOGIN: "/login",
  REGISTER: "/register",
  DASHBOARD: "/dashboard",

  CONTACTS: "contacts",
  MESSAGES: "messages",
  TASKS: "tasks",
  SAVED_VIEWS: "saved-views",
  METRICS: "metrics",
  FUNNEL: "funnel",
  SALESPERSONS: "salespersons",
  REPORTS: "reports",
  TEMPLATES: "templates",
  TAGS: "tags",

  HOME: "/",
} as const;

export const ROUTE_BUILDERS = {
  salespersonDetail: (userId: string) => `/dashboard/salespersons/${userId}`,
  contactDetail: (contactId: string) => `/dashboard/contacts/${contactId}`,
  messageDetail: (messageId: string) => `/dashboard/messages/${messageId}`,
  taskDetail: (taskId: string) => `/dashboard/tasks/${taskId}`,
  savedViewDetail: (viewId: string) => `/dashboard/saved-views/${viewId}`,
  metricDetail: (metricId: string) => `/dashboard/metrics/${metricId}`,
  reportDetail: (reportId: string) => `/dashboard/reports/${reportId}`,
  templateDetail: (templateId: string) => `/dashboard/templates/${templateId}`,
} as const;



