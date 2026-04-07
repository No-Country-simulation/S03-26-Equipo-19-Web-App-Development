package com.crm.app.dto;

import com.crm.app.model.enums.EntityType;
import com.crm.app.model.enums.SortOrder;
import lombok.Data;

@Data
public class SavedViewRequest {

    private String name;
    private String filters;
    private EntityType entity;
    private String sortBy;
    private SortOrder sortOrder;
    private boolean global;
}