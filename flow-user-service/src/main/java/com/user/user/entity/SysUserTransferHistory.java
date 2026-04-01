package com.user.user.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_transfer_history")
public class SysUserTransferHistory extends BaseEntity {
    private Long userId;
    private Long fromDeptId;
    private Long toDeptId;
    private Long fromPositionId;
    private Long toPositionId;
    private LocalDate transferDate;
    private String reason;
    private Long operatorId;
}
