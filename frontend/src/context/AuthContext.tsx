import { createContext } from 'react';

export interface User {
  id: string;
  name: string;
  email: string;
  role: 'admin' | 'agent';
}

export interface AuthContextType {
  user: User | null;
  login: (user: User) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | null>(null);