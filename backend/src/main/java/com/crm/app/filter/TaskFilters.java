package com.crm.app.filter;

import com.crm.app.model.enums.TaskStatus;
import com.crm.app.model.enums.TaskType;
import java.time.LocalDate;

import lombok.Data;

@Data
public class TaskFilters {

    private TaskStatus status;
    private TaskType type;
    private Long assignedTo;
    private Long contactId;
    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;

    // getters/setters
}