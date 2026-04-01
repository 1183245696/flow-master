package com.flow.workflow.event;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ProcessTaskEvent implements Serializable {
    private String processInstanceId;
    private String taskId;
    private Long nextAssignee;
    private String processName;
    private Long initiatorId;
    private LocalDateTime occurredAt = LocalDateTime.now();
}
