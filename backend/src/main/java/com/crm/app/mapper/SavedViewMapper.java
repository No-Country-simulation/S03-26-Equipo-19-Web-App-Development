package com.crm.app.mapper;

import com.crm.app.dto.SavedViewRequest;
import com.crm.app.dto.SavedViewResponse;
import com.crm.app.model.SavedView;
import com.crm.app.model.User;

public class SavedViewMapper {

    public static SavedView toEntity(SavedViewRequest request, User user) {
        return SavedView.builder()
                .name(request.getName())
                .filters(request.getFilters())
                .entity(request.getEntity())
                .sortBy(request.getSortBy())
                .sortOrder(request.getSortOrder())
                .global(request.isGlobal())
                .user(user)
                .build();
    }

    public static SavedViewResponse toResponse(SavedView view) {
        return SavedViewResponse.builder()
                .id(view.getId())
                .name(view.getName())
                .filters(view.getFilters())
                .entity(view.getEntity())
                .sortBy(view.getSortBy())
                .sortOrder(view.getSortOrder())
                .global(view.isGlobal())
                .userId(view.getUser().getId())
                .userName(view.getUser().getName())
                .createdAt(view.getCreatedAt())
                .updatedAt(view.getUpdatedAt())
                .build();
    }
}