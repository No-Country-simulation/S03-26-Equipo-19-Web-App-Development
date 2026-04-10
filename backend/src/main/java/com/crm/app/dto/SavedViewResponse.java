package com.crm.app.dto;

import com.crm.app.model.enums.EntityType;
import com.crm.app.model.enums.SortOrder;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavedViewResponse {

    private Long id;
    private String name;
    private JsonNode filters;
    private EntityType entity;
    private String sortBy;
    private SortOrder sortOrder;
    private boolean global;

    private Long userId;
    private String userName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}