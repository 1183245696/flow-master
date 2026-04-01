package com.flow.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUserAuth extends BaseEntity {
    private String username;
    private String password;
    private String roles;   // comma-separated: ROLE_ADMIN,ROLE_USER
    private Integer status; // 1=enabled, 0=disabled
}
