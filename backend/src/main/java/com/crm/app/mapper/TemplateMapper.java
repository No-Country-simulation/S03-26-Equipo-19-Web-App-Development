package com.crm.app.mapper;

import com.crm.app.dto.TemplateDTOs;
import com.crm.app.model.Template;
import com.crm.app.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface TemplateMapper {

    TemplateMapper INSTANCE = Mappers.getMapper(TemplateMapper.class);

    @Mapping(target = "variables", source = "variables", qualifiedByName = "jsonToMap")
    @Mapping(target = "createdBy", source = "createdBy")
    TemplateDTOs.TemplateResponse toResponse(Template template);

    @Mapping(target = "role", source = "user", qualifiedByName = "userRoleToString")
    TemplateDTOs.CreatorInfo toCreatorInfo(User user);

    List<TemplateDTOs.TemplateResponse> toResponseList(List<Template> templates);

    @Named("jsonToMap")
    @SuppressWarnings("unchecked")
    static Map<String, String> jsonToMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    @Named("userRoleToString")
    default String userRoleToString(User user) {
        return user != null && user.getRole() != null ? user.getRole().name() : null;
    }
}