// Esquema de validación y tipado para el usuario autenticado
import { z } from "zod";

export const userSchema = z
.object({
  id: z.string().uuid(), 
  name: z.string(), 
  lastName: z.string(), 
  email: z.email(), 
  rol: z.enum(["admin", "sales"]), 
  token: z.string(), 
})
 .strip();

