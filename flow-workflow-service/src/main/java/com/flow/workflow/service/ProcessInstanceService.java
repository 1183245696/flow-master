package com.flow.workflow.service;

import com.flow.base.exception.BusinessException;
import com.flow.workflow.event.ProcessTaskEvent;
import com.flow.workflow.mapper.ProcessDefinitionVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceService {

    private static final String WORKFLOW_EXCHANGE = "workflow.events";

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final ProcessDefinitionVersionMapper versionMapper;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public ProcessInstance startProcess(String processKey, Long initiatorId, Map<String, Object> variables) {
        variables = variables != null ? variables : new HashMap<>();
        variables.put("initiatorId", initiatorId);
        variables.put("startUserId", String.valueOf(initiatorId));

        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey(processKey, variables);

        // Publish task event for first task
        publishNextTaskEvent(instance.getId(), initiatorId,
                getProcessName(processKey));
        return instance;
    }

    @Transactional
    public void approveTask(String taskId, Long userId, String comment, Map<String, Object> variables) {
        Task task = getAndValidateTask(taskId, userId);
        String processInstanceId = task.getProcessInstanceId();
        String processName = task.getProcessDefinitionId();

        if (comment != null) {
            taskService.addComment(taskId, processInstanceId, comment);
        }
        if (variables != null) {
            taskService.setVariables(taskId, variables);
        }
        taskService.complete(taskId);

        // Publish event for next assignee
        publishNextTaskEvent(processInstanceId, userId, processName);
    }

    @Transactional
    public void rejectTask(String taskId, Long userId, String comment) {
        Task task = getAndValidateTask(taskId, userId);
        String processInstanceId = task.getProcessInstanceId();

        taskService.addComment(taskId, processInstanceId, "驳回: " + comment);

        Map<String, Object> vars = new HashMap<>();
        vars.put("approved", false);
        taskService.complete(taskId, vars);

        publishNextTaskEvent(processInstanceId, userId, task.getProcessDefinitionId());
    }

    private Task getAndValidateTask(String taskId, Long userId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskAssignee(String.valueOf(userId))
                .singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或您无权操作此任务");
        }
        return task;
    }

    private void publishNextTaskEvent(String processInstanceId, Long initiatorId, String processName) {
        List<Task> nextTasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();

        Long nextAssignee = null;
        String nextTaskId = null;
        if (!nextTasks.isEmpty()) {
            Task next = nextTasks.get(0);
            nextTaskId = next.getId();
            nextAssignee = next.getAssignee() != null
                    ? Long.parseLong(next.getAssignee()) : null;
        }

        ProcessTaskEvent event = new ProcessTaskEvent();
        event.setProcessInstanceId(processInstanceId);
        event.setTaskId(nextTaskId);
        event.setNextAssignee(nextAssignee);
        event.setProcessName(processName);
        event.setInitiatorId(initiatorId);

        rabbitTemplate.convertAndSend(WORKFLOW_EXCHANGE, "workflow.task.completed", event);
        log.info("Published ProcessTaskEvent processInstanceId={}, nextAssignee={}", processInstanceId, nextAssignee);
    }

    private String getProcessName(String processKey) {
        var versions = versionMapper.selectVersionHistory(processKey);
        return versions.isEmpty() ? processKey : versions.get(0).getName();
    }

    public List<Task> getMyPendingTasks(Long userId) {
        return taskService.createTaskQuery()
                .taskAssignee(String.valueOf(userId))
                .orderByTaskCreateTime().desc()
                .list();
    }

    public ProcessInstance getInstance(String instanceId) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId)
                .singleResult();
    }
}
