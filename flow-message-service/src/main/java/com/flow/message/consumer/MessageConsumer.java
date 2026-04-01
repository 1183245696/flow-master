package com.flow.message.consumer;

import com.flow.message.entity.MsgMessage;
import com.flow.message.service.MessageService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private final MessageService messageService;

    /**
     * 消费 workflow.events — 为 nextAssignee 创建 WORKFLOW 类型消息
     */
    @RabbitListener(queues = "workflow.message.queue")
    public void onWorkflowTaskEvent(ProcessTaskEventDTO event) {
        if (event.getNextAssignee() == null) {
            log.debug("No nextAssignee for processInstanceId={}, skip", event.getProcessInstanceId());
            return;
        }
        MsgMessage msg = new MsgMessage();
        msg.setUserId(event.getNextAssignee());
        msg.setTitle("您有新的待审批流程：" + event.getProcessName());
        msg.setContent("流程实例：" + event.getProcessInstanceId() + "，请及时处理。");
        msg.setCategory("WORKFLOW");
        msg.setSourceId(event.getProcessInstanceId());
        msg.setSourceType("process_instance");
        messageService.createAndPush(msg);
        log.info("Created WORKFLOW message for userId={}", event.getNextAssignee());
    }

    /**
     * 消费 user.events — 系统通知消息
     */
    @RabbitListener(queues = "user.message.queue")
    public void onUserCreatedEvent(UserCreatedEventDTO event) {
        MsgMessage msg = new MsgMessage();
        msg.setUserId(event.getUserId());
        msg.setTitle("欢迎加入！");
        msg.setContent("您的账号 " + event.getUsername() + " 已创建成功，欢迎使用 OA 平台。");
        msg.setCategory("SYSTEM");
        msg.setSourceId(String.valueOf(event.getUserId()));
        msg.setSourceType("user");
        messageService.createAndPush(msg);
        log.info("Created SYSTEM welcome message for userId={}", event.getUserId());
    }

    // ── Inner DTOs (mirror the event fields) ─────────────────────────────────

    @Data
    public static class ProcessTaskEventDTO {
        private String processInstanceId;
        private String taskId;
        private Long nextAssignee;
        private String processName;
        private Long initiatorId;
        private LocalDateTime occurredAt;
    }

    @Data
    public static class UserCreatedEventDTO {
        private Long userId;
        private String username;
        private String realName;
        private Long deptId;
        private LocalDateTime occurredAt;
    }
}
