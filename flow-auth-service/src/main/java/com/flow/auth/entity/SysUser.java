package com.flow.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String employeeNo;
    private Long deptId;
    private Long positionId;
    /** 0=正常 1=禁用 */
    private Integer status;
    private String avatar;
}
