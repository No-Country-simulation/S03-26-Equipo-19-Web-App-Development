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

        // Si data ya trae el usuario validado del backend, esto está perfecto
        const parsed = userSchema.parse(data);

        set({
          user: parsed,
          token: data.token,
          isAuthenticated: true,
          loading: false, // Asegura que el spinner desaparezca
        });
      } catch (err) {
        console.error("Error detectado:", err);
        set({
          error: 'Error al iniciar sesión. Revisa tus credenciales.',
          loading: false, // Si falla, el botón debe volver a ser clickable
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

