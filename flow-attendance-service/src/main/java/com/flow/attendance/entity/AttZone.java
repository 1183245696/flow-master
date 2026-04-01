package com.flow.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("att_zone")
public class AttZone extends BaseEntity {
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    /** 有效打卡半径（米） */
    private Integer radiusMeters;
}
