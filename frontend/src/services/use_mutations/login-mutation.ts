import { useMutation } from "@tanstack/react-query"; // Para useMutation
import { useNavigate } from "react-router-dom";     // Para useNavigate
import { useAuthStore } from "../../store/useAuthStore"; // Para useAuthStore (verifica que la ruta sea correcta)
import { postLogin } from "../use_cases/login-service";
import type { LoginType } from "../../types/auth.types";

interface ApiError {
  response?: {
    data?: {
      message?: string;
    };
  };
}

export const LoginMutationsService = () => {
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login); // Cambié el nombre para que sea más claro

  const mutationPostLogin = useMutation({
    mutationFn: (data: LoginType) => {
      return postLogin(data);
    },
onSuccess: (res) => {
  console.log("DATOS DE LA API:", res); // <-- ESTO ES CLAVE
  console.log("ROL RECIBIDO:", res.role); 

  login({
    id: res.id,
    name: res.name,
    email: res.email,
    role: res.role,
    token: res.token,
  });

  // Forzamos una validación más flexible por si acaso
  const isActuallyAdmin = String(res.role).toUpperCase() === "ADMIN";

  if (isActuallyAdmin) {
    console.log("Entrando como ADMIN...");
    navigate("/dashboard/admin"); 
  } else {
    console.log("Entrando como Vendedor...");
    navigate("/dashboard");
  }
},
    onError: (error: unknown) => { // Usamos unknown por seguridad
          const apiError = error as ApiError; // Lo casteamos a nuestra interface
          console.error("Login error:", apiError.response?.data?.message || "Error desconocido");
        },
  });

  return {
    mutationPostLogin,
  };
};