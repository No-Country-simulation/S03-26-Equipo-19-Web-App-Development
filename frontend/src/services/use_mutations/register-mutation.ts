import { useMutation } from "@tanstack/react-query";

import { useNavigate } from "react-router-dom";
import type { RegisterType } from "../../types/auth.types";
import { postRegister } from "../use_cases/register-service";
import { ROUTES } from "../../constants/routes";
import { useAuthStore } from "../../store/useAuthStore";

export const RegisterMutationsService = () => {
 const navigate = useNavigate()
  const userData = useAuthStore((state) => state.login);

  const mutationPostRegister = useMutation({
    mutationFn: (data: RegisterType) => {
      return postRegister(data);
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
  });

  return {
    mutationPostRegister,
  };
};
