package com.flow.org.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_position")
public class SysPosition extends BaseEntity {
    private String name;
    private Long deptId;
    private Integer sort;
}
