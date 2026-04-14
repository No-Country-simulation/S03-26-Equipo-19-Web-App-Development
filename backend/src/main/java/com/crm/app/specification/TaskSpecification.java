package com.crm.app.specification;

import com.crm.app.model.Task;
import com.crm.app.model.enums.TaskStatus;
import com.crm.app.model.enums.TaskType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TaskSpecification {

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasType(TaskType type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Task> hasAssignedTo(Long userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("assignedTo").get("id"), userId);
    }

    public static Specification<Task> dueDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null)
                return cb.between(root.get("dueDate"), from, to);
            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("dueDate"), from);
            return cb.lessThanOrEqualTo(root.get("dueDate"), to);
        };
    }
}