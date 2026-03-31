// Store global de usuario usando Zustand y persistencia local
import { create } from "zustand";
import { persist } from "zustand/middleware";
import { userSchema } from "../schemas/user_schema";
import type { User } from "../types/user.types";


// Interfaz del estado global de usuario
interface UserState extends Partial<User> {
  hasHydrated: boolean;
  setUserData: (data: Partial<User>) => void;
  clearUserData: () => void;
  setHasHydrated: (state: boolean) => void;
}

// Store Zustand con persistencia y validación Zod
export const useUserStore = create<UserState>()(

  persist(
    (set) => ({
      id: "",
      name: "",
      email: "",
      role: undefined,
      hasHydrated: false,

 
      setUserData: (data) => {
        let userData = data;

        if (
          typeof data === "object" &&
          data !== null &&
          "user" in data &&
          typeof (data as any).user === "object" &&
          (data as any).user !== null &&
          "access_token" in data
        ) {
          userData = {
            ...(data as any).user,
            token: (data as any).access_token,
          };
        }

        const parsed = userSchema.partial().safeParse(userData);

        if (parsed.success) {
          set((state) => ({
            ...state,
            ...parsed.data, 
          }));
        } else {
          console.error("❌ User data invalid:", parsed.error);
        }
      },

      clearUserData: () =>
        set({
          id: "",
          name: "",
          email: "",
          role: undefined,
        }),
        

      // Marca el store como hidratado desde el storage
      setHasHydrated: (state) => set({ hasHydrated: state }),
    }),
    {
      name: "user-data-storage", 
      onRehydrateStorage: () => (state) => {
        state?.setHasHydrated(true);
      },
    }
  )
);
