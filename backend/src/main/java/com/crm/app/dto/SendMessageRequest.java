package com.crm.app.dto;

import com.crm.app.model.enums.Channel;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SendMessageRequest(

        @NotNull
        Long contactId,

        @NotNull
        Channel channel,

        MessageDTOs.MessageContent content,

        Long templateId,

        Map<String, String> variables
) {}