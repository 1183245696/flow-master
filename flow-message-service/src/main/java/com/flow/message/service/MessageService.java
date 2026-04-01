package com.flow.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.message.entity.MsgMessage;
import com.flow.message.mapper.MsgMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService extends ServiceImpl<MsgMessageMapper, MsgMessage> {

    private final MsgMessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Save message and push via WebSocket to user
     */
    @Transactional
    public MsgMessage createAndPush(MsgMessage message) {
        message.setIsRead(false);
        save(message);

        // Push to user's private topic
        int unreadCount = messageMapper.countUnread(message.getUserId());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getUserId()),
                "/messages",
                Map.of(
                        "messageId", message.getId(),
                        "title", message.getTitle(),
                        "category", message.getCategory(),
                        "unreadCount", unreadCount
                )
        );
        log.info("Pushed message to userId={}, unread={}", message.getUserId(), unreadCount);
        return message;
    }

    public Page<MsgMessage> pageMessages(Long userId, String category,
                                          int pageNum, int pageSize) {
        LambdaQueryWrapper<MsgMessage> wrapper = new LambdaQueryWrapper<MsgMessage>()
                .eq(MsgMessage::getUserId, userId)
                .eq(StringUtils.hasText(category), MsgMessage::getCategory, category)
                .orderByDesc(MsgMessage::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Transactional
    public void markRead(Long messageId, Long userId) {
        update(new LambdaUpdateWrapper<MsgMessage>()
                .eq(MsgMessage::getId, messageId)
                .eq(MsgMessage::getUserId, userId)
                .set(MsgMessage::getIsRead, true));
    }

    public int countUnread(Long userId) {
        return messageMapper.countUnread(userId);
    }

    /**
     * Push current unread count on WebSocket connect
     */
    public void pushUnreadCount(Long userId) {
        int count = messageMapper.countUnread(userId);
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/messages",
                Map.of("unreadCount", count)
        );
    }
}
