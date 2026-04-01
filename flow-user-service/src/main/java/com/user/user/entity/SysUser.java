package com.user.user.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    /** 分片键 — 与 id 保持一致 */
    private Long userId;
    private String username;
    private String password;
    private String realName;
    private String employeeNo;
    private Long deptId;
    private Long positionId;
    private String phone;
    private String email;
    private String avatar;
    /** 1=在职, 0=离职 */
    private Integer status;
    private LocalDate hireDate;
    private String roles;
}
