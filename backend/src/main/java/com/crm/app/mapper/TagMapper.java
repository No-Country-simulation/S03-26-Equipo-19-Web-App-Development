package com.crm.app.mapper;

import com.crm.app.dto.TagDTOs;
import com.crm.app.model.Tag;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TagMapper {

    public TagDTOs.TagResponse toResponse(Tag tag) {
        return new TagDTOs.TagResponse(
                tag.getId(),
                tag.getName(),
                tag.getColor(),
                tag.getCreatedAt()
        );
    }

    public List<TagDTOs.TagResponse> toResponseList(List<Tag> tags) {
        return tags.stream().map(this::toResponse).toList();
    }
}