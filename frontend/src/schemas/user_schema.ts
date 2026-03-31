// Esquema de validación y tipado para el usuario autenticado
import { z } from "zod";

export const userSchema = z
.object({
  id: z.number(), 
  name: z.string(), 
  email: z.email(), 
  role: z.enum(["ADMIN", "SALESPERSON"]), 
  token: z.string(), 
})
 .strip();

