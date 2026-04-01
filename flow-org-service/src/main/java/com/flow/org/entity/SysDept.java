package com.flow.org.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {
    private String name;
    private Long parentId;
    private Long leaderUserId;
    /** 关联考勤区域；null 时需继承祖先的 zone */
    private Long attendanceZoneId;
    private Long ruleId;
    /** 路径，如 /1/2/3/ */
    private String path;
    private Integer level;
    private Integer sort;
}
