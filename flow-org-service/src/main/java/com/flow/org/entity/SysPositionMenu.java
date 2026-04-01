package com.flow.org.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_position_menu")
public class SysPositionMenu {
    private Long positionId;
    private Long menuId;
}
