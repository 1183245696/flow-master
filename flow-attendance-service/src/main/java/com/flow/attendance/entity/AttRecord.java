package com.flow.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("att_record")
public class AttRecord extends BaseEntity {
    /** 分片键 */
    private Long userId;
    private Long deptId;
    private LocalDateTime clockTime;
    private Double lat;
    private Double lng;
    private Long zoneId;
    /** IN | OUT */
    private String type;
    private Integer distanceMeters;
    private Boolean isValid;
}
