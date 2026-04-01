package com.flow.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("msg_message")
public class MsgMessage extends BaseEntity {
    private Long userId;
    private String title;
    private String content;
    /** WORKFLOW | SYSTEM | NOTICE */
    private String category;
    private String sourceId;
    private String sourceType;
    private Boolean isRead;
}
