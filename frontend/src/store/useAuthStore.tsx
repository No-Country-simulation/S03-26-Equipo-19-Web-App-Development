import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { userSchema } from '../schemas/user_schema';
import type { AuthState } from '../types/auth.types';


export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      loading: false,
      error: null,

      login: async (data) => {
        try {
          set({ loading: true, error: null });

          // Validación con Zod
          const parsed = userSchema.parse(data);

          set({
            user: parsed,
            token: data.token,
            isAuthenticated: true,
            loading: false,
          });
        } catch (err) {
          console.error(err);
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

