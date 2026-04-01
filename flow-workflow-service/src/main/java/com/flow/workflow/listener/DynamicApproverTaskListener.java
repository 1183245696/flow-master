package com.flow.workflow.listener;

import com.flow.workflow.entity.SysProcessApproverConfig;
import com.flow.workflow.mapper.SysProcessApproverConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Flowable TaskListener — resolves actual approver at task creation time
 * from sys_process_approver_config.
 *
 * Configure in BPMN:
 * <extensionElements>
 *   <flowable:taskListener event="create"
 *       delegateExpression="${dynamicApproverTaskListener}" />
 * </extensionElements>
 */
@Slf4j
@Component("dynamicApproverTaskListener")
@RequiredArgsConstructor
public class DynamicApproverTaskListener implements TaskListener {

    private final SysProcessApproverConfigMapper configMapper;

    @Override
    public void notify(DelegateTask delegateTask) {
        String processKey = delegateTask.getProcessDefinitionId().split(":")[0];
        String nodeId     = delegateTask.getTaskDefinitionKey();

        List<SysProcessApproverConfig> configs =
                configMapper.selectByProcessKeyAndNodeId(processKey, nodeId);

        if (configs.isEmpty()) {
            log.warn("No approver config for processKey={}, nodeId={}", processKey, nodeId);
            return;
        }

        SysProcessApproverConfig cfg = configs.get(0);
        String assignee = resolveAssignee(cfg, delegateTask);

        if (assignee != null) {
            delegateTask.setAssignee(assignee);
            log.info("Resolved assignee={} for task={} processKey={}", assignee, nodeId, processKey);
        }
    }

    private String resolveAssignee(SysProcessApproverConfig cfg, DelegateTask task) {
        return switch (cfg.getApproverType()) {
            case "user" -> cfg.getApproverValue();
            case "position" -> {
                // In real impl: query user with this positionId in the initiator's dept
                Object initiatorId = task.getVariable("initiatorId");
                yield initiatorId != null ? initiatorId.toString() : cfg.getApproverValue();
            }
            case "dept_leader" -> {
                // In real impl: call OrgRpcService to get dept leader
                Object initiatorId = task.getVariable("initiatorId");
                yield initiatorId != null ? initiatorId.toString() : null;
            }
            default -> {
                log.warn("Unknown approverType: {}", cfg.getApproverType());
                yield null;
            }
        };
    }
}
