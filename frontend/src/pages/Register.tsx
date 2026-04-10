import React, { useState } from "react";
import { Link } from "react-router-dom";
import { Input } from "../components/ui/Input";
import { ROUTES } from "../constants/routes";
import { Button } from "../components/ui/Button";
import { Eye, EyeClosed, Lock } from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { registerformSchema, type RegisterFormValues } from "../schemas/registerForm_schema";
import { RegisterMutationsService } from "../services/use_mutations/register-mutation";


const Register: React.FC = () => {

  const [showPassword, setShowPassword] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerformSchema),
  });

  const { mutationPostRegister } = RegisterMutationsService();

  // Handler para enviar el formulario y disparar la mutación
  const onSubmit = (data: RegisterFormValues) => {
    mutationPostRegister.mutate(data);
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-neutro-1">
      <div className="w-full max-w-md">
        <div className="bg-white backdrop-blur-xl border border-slate-800 p-8 rounded-2xl shadow-2xl">
          <div className="flex flex-col items-center mb-8">
            <div className="w-12 h-12 bg-secondary rounded-xl flex items-center justify-center mb-4 shadow-lg shadow-indigo-600/20">
              <Lock className="w-7 h-7 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-secondary mb-1">Registrate</h1>
            <p className="text-neutro-1">Ingresá tus datos para comenzar.</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid md:grid-cols-2 gap-4">
              <Input
                label="Nombre"
                placeholder="Juan"
                type="text"
                {...register("name")}
                error={errors.name?.message}
              />
              <Input
                label="Email"
                placeholder="ejemplo@gmail.com"
                type="email"
                {...register("email")}
                error={errors.email?.message}
                containerClassName="col-span-1 md:col-span-2"
              />

              <div className="relative">
                <Input
                  label="Contraseña"
                  placeholder="••••••••"
                  type={showPassword ? "text" : "password"}
                  {...register("password")}
                  error={errors.password?.message}
                />

                {showPassword ? (
                  <EyeClosed
                    onClick={() => setShowPassword(false)}
                    className="absolute top-8.5 right-3 w-4 cursor-pointer"
                  />
                ) : (
                  <Eye
                    onClick={() => setShowPassword(true)}
                    className="absolute top-8.5 right-3 w-4 cursor-pointer"
                  />
                )}
              </div>
              <div className="relative">
                <Input
                  label="Confirmar contraseña"
                  placeholder="••••••••"
                  type={showPassword ? "text" : "password"}
                  {...register("confirmPass")}
                  error={errors.confirmPass?.message}
                />

                {showPassword ? (
                  <EyeClosed
                    onClick={() => setShowPassword(false)}
                    className="absolute top-8.5 right-3 w-4 cursor-pointer"
                  />
                ) : (
                  <Eye
                    onClick={() => setShowPassword(true)}
                    className="absolute top-8.5 right-3 w-4 cursor-pointer"
                  />
                )}
              </div>
            </div>
            {mutationPostRegister.isError && (
              <p className="text-red-500 text-sm text-center">
                {(mutationPostRegister.error as any)?.response?.data?.message ||
                  "Credenciales incorrectas"}
              </p>
            )}
            <Button type="submit" variant="primary" className="w-full mt-4" disabled={mutationPostRegister.isPending}>
              {mutationPostRegister.isPending ? "Registrando..." : "Registrate"}
            </Button>
          </form>

          <div className="mt-8 pt-6 border-t border-neutro-2 text-center">
            <p className="text-neutro-2 text-sm">
              ¿Ya tenés cuenta?{" "}
              <Link
                to={ROUTES.LOGIN}
                className="text-accent hover:text-secondary font-semibold transition-colors"
              >
                Iniciá sesión
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;