import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "../../store/useAuthStore";
import { getMessages } from "../use_cases/messages-service";
import type { MessageResType } from "../../types/message.types";

export const useGetMessages = () => {
  const token = useAuthStore((state) => state.token);
  return useQuery<MessageResType[]>({
    queryKey: ["messages"],
    queryFn: () => getMessages(token!),
    enabled: !!token,
  });
};

