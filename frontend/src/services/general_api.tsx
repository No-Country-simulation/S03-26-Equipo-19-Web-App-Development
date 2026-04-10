import axios from "axios";

export const apiAuthService = axios.create({
    baseURL: `${import.meta.env.VITE_URL_BASE}/auth`
})

export const apiContactsService = axios.create({
    baseURL: `${import.meta.env.VITE_URL_BASE}/contacts`
})

export const apiMessagesService = axios.create({
    baseURL: `${import.meta.env.VITE_URL_BASE}/messages`
})

export const apiSalespersonService = axios.create({
    baseURL: `${import.meta.env.VITE_URL_BASE}/salespersons`
})

export const apiTemplatesService = axios.create({
    baseURL: `${import.meta.env.VITE_URL_BASE}/templates`
})

export const apiConversationsService = axios.create({
    baseURL: `${import.meta.env.VITE_URL_BASE}/conversations`
})

export const apiWebhooksService = axios.create({
    baseURL: `${import.meta.env.VITE_URL_BASE}/webhooks`
})


