package com.crm.app.mapper;

import com.crm.app.dto.ConversationDTOs;
import com.crm.app.model.Conversation;
import com.crm.app.model.Contact;
import com.crm.app.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    ConversationMapper INSTANCE = Mappers.getMapper(ConversationMapper.class);

    @Mapping(target = "contact", source = "contact")
    @Mapping(target = "assignedTo", source = "assignedTo")
    ConversationDTOs.ConversationResponse toResponse(Conversation conversation);

    ConversationDTOs.ContactInfo toContactInfo(Contact contact);

    ConversationDTOs.UserInfo toUserInfo(User user);

    List<ConversationDTOs.ConversationResponse> toResponseList(List<Conversation> conversations);
}