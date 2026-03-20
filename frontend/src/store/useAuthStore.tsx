import { create } from 'zustand';
import { persist } from 'zustand/middleware';

type User = {
  id: string;
  name: string;
};

type AuthState = {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;

  loading: boolean;
  error: string | null;

  login: (user: User, token: string) => Promise<void>;
  logout: () => void;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,

      loading: false,
      error: null,

      login: async (user, token) => {
        try {
          set({ loading: true, error: null });

          await new Promise((res) => setTimeout(res, 500));

          set({
            user,
            token,
            isAuthenticated: true,
            loading: false,
          });
        } catch {
          set({
            error: 'Login failed',
            loading: false,
          });
        }
      },

      logout: () =>
        set({
          user: null,
          token: null,
          isAuthenticated: false,
        }),
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);