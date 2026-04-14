package com.crm.app.mapper;

import com.crm.app.dto.TaskDTOs;
import com.crm.app.model.Task;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskMapper {

    public TaskDTOs.TaskResponse toResponse(Task task) {
        Long contactId = task.getContact() != null ? task.getContact().getId() : null;
        Long assignedToId = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;

        return new TaskDTOs.TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getType(),
                task.getStatus(),
                task.getDueDate(),
                task.getCompletedAt(),
                contactId,
                assignedToId,
                task.getCreatedAt()
        );
    }

    public List<TaskDTOs.TaskResponse> toResponseList(List<Task> tasks) {
        return tasks.stream()
                .map(this::toResponse)
                .toList();
    }
}