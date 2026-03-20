import { z } from "zod";

export const registerformSchema = z
  .object({
    name: z.string().min(3, {
      message: "El nombre de usuario debe tener al menos 3 caracteres",
    }),
    lastName: z.string().min(3, {
      message: "El apellido de usuario debe tener al menos 3 caracteres",
    }),
    email: z.string().email({
      message: "Tu correo electrónico no es válido",
    }),
    password: z
      .string()
      .min(6, { message: "La contraseña debe tener al menos 6 caracteres" })
      .max(8, { message: "La contraseña no puede superar los 8 caracteres" })
      .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{6,8}$/, {
        message: "La contraseña debe incluir mayúsculas, minúsculas y números",
      }),
    confirmPass: z.string({
      message: "Debe confirmar la contraseña",
    }),
  })
  .refine((data) => data.password === data.confirmPass, {
    message: "Las contraseñas no coinciden",
    path: ["confirmpassword"], // el error aparece en el campo de confirmación
  });


  export type RegisterFormValues = z.infer<typeof registerformSchema>;