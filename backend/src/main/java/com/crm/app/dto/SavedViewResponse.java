package com.crm.app.dto;

import com.crm.app.model.enums.EntityType;
import com.crm.app.model.enums.SortOrder;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SavedViewResponse {

    private Long id;
    private String name;
    private String filters;
    private EntityType entity;
    private String sortBy;
    private SortOrder sortOrder;
    private boolean global;

    private Long userId;
    private String userName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}