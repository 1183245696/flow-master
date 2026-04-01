package com.flow.org.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    private String name;
    private String path;
    private String component;
    private String icon;
    /** menu | button */
    private String type;
    private Long parentId;
    private Integer sort;
    /** HTTP method for button-type permissions, e.g. GET/POST */
    private String method;
}
