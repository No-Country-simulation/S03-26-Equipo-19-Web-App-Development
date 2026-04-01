import type { User } from "./user.types";

export interface LoginType{
    email: string,
    password: string,
}

export interface RegisterType{
    name: string,
    email: string,
    password: string,
    confirmPass?: string
}

export type AuthState = {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;

  loading: boolean;
  error: string | null;

  login: (data: any) => Promise<void>;
  logout: () => void;
};

