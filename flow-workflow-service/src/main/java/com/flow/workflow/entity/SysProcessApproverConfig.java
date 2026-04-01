package com.flow.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_process_approver_config")
public class SysProcessApproverConfig extends BaseEntity {
    private String processKey;
    private String nodeId;
    /** user | position | dept_leader */
    private String approverType;
    /** userId / positionId / deptId depending on type */
    private String approverValue;
}
