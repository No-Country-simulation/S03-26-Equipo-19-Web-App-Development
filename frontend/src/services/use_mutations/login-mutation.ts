import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { useUserStore } from "../../store/useUserStore";
import type { LoginType } from "../../types/auth.types";
import { postLogin } from "../use_cases/login-service";
import { ROUTES } from "../../constants/routes";

export const LoginMutationsService = () => {
  const navigate = useNavigate();
  const setUserData = useUserStore((state) => state.setUserData);

  const mutationPostLogin = useMutation({
    mutationFn: (data: LoginType) => {
      return postLogin(data);
    },
    onSuccess: function Exito(_res) {
      setUserData({
      /*   id: _res.user.id,
        name: _res.user.name, */
        email: _res.email,
        role: _res.role,
        token: _res.token,
      });
      console.log("Login executionAsyncResource, deberia redirigir");
      
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
