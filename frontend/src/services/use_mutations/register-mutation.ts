import { useMutation } from "@tanstack/react-query";
import { useUserStore } from "../../store/useUserStore";
import { useNavigate } from "react-router-dom";
import type { RegisterType } from "../../types/auth.types";
import { postRegister } from "../use_cases/register-service";
import { ROUTES } from "../../constants/routes";

export const RegisterMutationsService = () => {
 const navigate = useNavigate()
  const setUserData = useUserStore((state) => state.setUserData);

  const mutationPostRegister = useMutation({
    mutationFn: (data: RegisterType) => {
      return postRegister(data);
    },
    onSuccess: function Exito(_res) {
      setUserData({
        id: _res.user.id,
        name: _res.user.name,
        lastName: _res.user.lastName,
        email: _res.user.email,
        rol: _res.user.rol,
        token: _res.access_token,
      });
       navigate(ROUTES.DASHBOARD);
    },
  });

  return {
    mutationPostRegister,
  };
};
