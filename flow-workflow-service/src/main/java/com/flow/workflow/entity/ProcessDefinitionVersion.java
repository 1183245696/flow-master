package com.flow.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_process_definition_version")
public class ProcessDefinitionVersion extends BaseEntity {
    private String processKey;
    private Integer version;
    private String deploymentId;
    private String processDefinitionId;
    private String name;
    private String categoryCode;
    private String description;
    private String icon;
    private String bpmnXml;
    /** 是否最新版本 */
    private Boolean isLatest;
    /** 是否固定（pinned）到首页 */
    private Boolean isPinned;
    private LocalDateTime deployedAt;
}
