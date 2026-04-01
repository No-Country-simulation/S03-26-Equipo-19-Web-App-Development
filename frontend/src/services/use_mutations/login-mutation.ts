import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import type { LoginType } from "../../types/auth.types";
import { postLogin } from "../use_cases/login-service";
import { ROUTES } from "../../constants/routes";
import { useAuthStore } from "../../store/useAuthStore";

export const LoginMutationsService = () => {
  const navigate = useNavigate();
  const userData = useAuthStore((state) => state.login);

  const mutationPostLogin = useMutation({
    mutationFn: (data: LoginType) => {
      return postLogin(data);
    },
    onSuccess: function Exito(_res) {
      userData({
        id: _res.id,
        name: _res.name,
        email: _res.email,
        role: _res.role,
        token: _res.token,
      });   
      navigate(ROUTES.DASHBOARD);
    },
    onError: (error: any) => {
      console.log("Login error:", error);
    },
  });

  return {
    mutationPostLogin,
  };
};
