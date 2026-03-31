import { z } from "zod";

export const loginFormSchema = z.object({
  email: z.string().email({
    message: "Tu correo electrónico no es válido",
  }),
  password: z
    .string()
    .min(6, { message: "La contraseña debe tener al menos 6 caracteres" })
    .max(10, { message: "La contraseña no puede superar los 10 caracteres" })
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,10}$/, {
      message:
        "La contraseña debe incluir mayúsculas, minúsculas, números y un carácter especial",
    }),
});

export type LoginFormValues = z.infer<typeof loginFormSchema>;