package com.flow.gateway.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * IP 黑/白名单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gateway_ip_list")
public class BlackWhiteListEntity extends BaseEntity {

    /** blacklist | whitelist */
    private String listType;

    /** IP 地址 */
    private String ipAddress;

    /** 备注 */
    private String remark;
}
