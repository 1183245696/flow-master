package com.flow.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flow.base.response.R;
import com.flow.message.entity.MsgMessage;
import com.flow.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "消息中心")
@RestController
@RequestMapping("/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "消息列表（按分类分页）")
    @GetMapping
    public R<Page<MsgMessage>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(messageService.pageMessages(userId, category, page, size));
    }

    @Operation(summary = "标记消息为已读")
    @PutMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id,
                             @RequestHeader("X-User-Id") Long userId) {
        messageService.markRead(id, userId);
        return R.ok();
    }

    @Operation(summary = "未读消息数量")
    @GetMapping("/unread-count")
    public R<Integer> unreadCount(@RequestHeader("X-User-Id") Long userId) {
        return R.ok(messageService.countUnread(userId));
    }
}
