package com.crm.app.mapper;

import com.crm.app.dto.MessageDTOs;
import com.crm.app.model.Conversation;
import com.crm.app.model.Message;
import com.crm.app.model.Template;
import com.crm.app.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageMapper INSTANCE = Mappers.getMapper(MessageMapper.class);

    @Mapping(target = "conversation", source = "conversation")
    @Mapping(target = "sender", source = "sender")
    @Mapping(target = "template", source = "template")
    MessageDTOs.MessageResponse toResponse(Message message);

    MessageDTOs.ConversationInfo toConversationInfo(Conversation conversation);

    MessageDTOs.SenderInfo toSenderInfo(User user);

    MessageDTOs.TemplateInfo toTemplateInfo(Template template);

    List<MessageDTOs.MessageResponse> toResponseList(List<Message> messages);
}