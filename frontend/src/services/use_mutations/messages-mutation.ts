import { useMutation } from "@tanstack/react-query";

import type { MessageReqType } from "../../types/message.types";
import { postMessage } from "../use_cases/messages-service";

export const MessagesMutationsService = () => {

  const mutationPostMessage = useMutation({
    mutationFn: (data: MessageReqType) => postMessage(data),
  });



  return {
    mutationPostMessage,

  };
};
