package com.flow.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_process_category")
public class SysProcessCategory extends BaseEntity {
    private String name;
    private String code;
    private Integer sort;
}
