package com.crm.app.mapper;

import com.crm.app.dto.ContactDTOs;
import com.crm.app.model.Contact;
import com.crm.app.model.Tag;
import com.crm.app.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContactMapper {

    ContactMapper INSTANCE = Mappers.getMapper(ContactMapper.class);

    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "tags", source = "tags")
    ContactDTOs.ContactDetailResponse toDetailResponse(Contact contact);

    @Mapping(target = "owner", source = "owner")
    ContactDTOs.ContactSummaryResponse toSummaryResponse(Contact contact);

    ContactDTOs.OwnerInfo toOwnerInfo(User user);

    ContactDTOs.TagInfo toTagInfo(Tag tag);

    List<ContactDTOs.ContactDetailResponse> toDetailResponseList(List<Contact> contacts);
}