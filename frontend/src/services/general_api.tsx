import axios from "axios";

export const apiAuthService = axios.create({
    baseURL: `${import.meta.env.VITE_URL_BASE}/auth`
})

export const apiUsersService = axios.create({
    baseURL: `${import.meta.env.VITE_URL_BASE}/users`
})

export const apiMessagesService = axios.create({
    baseURL: `${import.meta.env.VITE_URL_BASE}/messages`
})



