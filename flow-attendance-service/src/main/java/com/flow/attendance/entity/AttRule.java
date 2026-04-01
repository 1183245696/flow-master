package com.flow.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("att_rule")
public class AttRule extends BaseEntity {
    private String name;
    /** 双休 | 大小周 | 自定义 */
    private String type;
    /** JSON array of work day numbers, e.g. [1,2,3,4,5] */
    private String workDays;
    /** HH:mm */
    private String workStart;
    /** HH:mm */
    private String workEnd;
    /** 弹性打卡分钟数 */
    private Integer flexMinutes;
}
