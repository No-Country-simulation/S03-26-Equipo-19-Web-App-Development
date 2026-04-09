package com.crm.app.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FiltersMapper {

    private final ObjectMapper objectMapper;

    public ContactFilters toContactFilters(JsonNode node) {
        return objectMapper.convertValue(node, ContactFilters.class);
    }

    public TaskFilters toTaskFilters(JsonNode node) {
        return objectMapper.convertValue(node, TaskFilters.class);
    }
}