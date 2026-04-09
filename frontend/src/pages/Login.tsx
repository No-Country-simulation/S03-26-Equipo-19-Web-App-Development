import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Input } from "../components/ui/Input";
import { ROUTES } from "../constants/routes";
import { Button } from "../components/ui/Button";
import { Eye, EyeClosed, Lock } from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { loginFormSchema, type LoginFormValues } from "../schemas/loginForm_schema";
import { LoginMutationsService } from "../services/use_mutations/login-mutation";

// Definir un tipo para el error de la API para evitar el 'any'
interface ApiError {
  response?: {
    data?: {
      message?: string;
    };
  };
}

const Login: React.FC = () => {
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginFormSchema),
  });

  const { mutationPostLogin } = LoginMutationsService();

  const onSubmit = (data: LoginFormValues) => {
    mutationPostLogin.mutate(data, {
      onSuccess: (response) => {
        // Asumiendo que la respuesta tiene el usuario o el store se actualiza
        // Si el store se actualiza dentro de la mutación, leemos el rol:
        const userRole = response?.user?.role || response?.role; 
        
        if (userRole === "ADMIN") {
          navigate(ROUTES.ADMIN_DASHBOARD);
        } else {
          navigate(ROUTES.DASHBOARD);
        }
      }
    });
  };

  // Tipado correcto para el error
  const error = mutationPostLogin.error as ApiError;

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-neutro-1">
      <div className="w-full max-w-md">
        <div className="bg-white backdrop-blur-xl border border-slate-800 p-8 rounded-2xl shadow-2xl">
          <div className="flex flex-col items-center mb-8">
            <div className="w-12 h-12 bg-secondary rounded-xl flex items-center justify-center mb-4 shadow-lg shadow-indigo-600/20">
              <Lock className="w-7 h-7 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-secondary mb-1">Bienvenid@</h1>
            <p className="text-neutro-1">Ingresá tus credenciales para acceder.</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <Input
              label="Email"
              placeholder="ejemplo@gmail.com"
              type="email"
              {...register("email")}
              error={errors.email?.message}
            />

            <div className="relative">
              <Input
                label="Contraseña"
                placeholder="••••••••"
                type={showPassword ? "text" : "password"}
                {...register("password")}
                error={errors.password?.message}
              />
              <div 
                className="absolute top-9 right-3 cursor-pointer text-slate-500"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? <EyeClosed size={18} /> : <Eye size={18} />}
              </div>
            </div>

            <div className="flex items-center justify-between">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  className="w-4 h-4 rounded focus:ring-accent focus:ring-offset-secondary"
                />
                <span className="text-sm text-neutro-1">Recordarme</span>
              </label>
              <button
                type="button"
                className="text-sm text-accent hover:text-secondary font-medium transition-colors"
              >
                ¿Olvidaste tu contraseña?
              </button>
            </div>

            {mutationPostLogin.isError && (
              <p className="text-red-500 text-sm text-center">
                {error?.response?.data?.message || "Credenciales incorrectas"}
              </p>
            )}

            <div className="flex justify-center">
              <Button 
                type="submit" 
                variant="primary" 
                className="mt-4 w-1/2" 
                disabled={mutationPostLogin.isPending}
              >
                {mutationPostLogin.isPending ? "Ingresando..." : "Iniciar sesión"}
              </Button>
            </div>
          </form>

          {/* ... resto del componente igual */}
        </div>
      </div>
    </div>
  );
};

export default Login;