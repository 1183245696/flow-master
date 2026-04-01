package com.flow.attendance.rpc;

import java.time.LocalDateTime;

/**
 * 考勤 RPC 接口 — 供 Workflow 服务调用
 */
public interface AttendanceRpcService {

    /**
     * 计算请假时长（小时），排除非工作时间
     * 根据用户所在部门的打卡规则计算
     */
    double calculateLeaveDuration(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取部门对应考勤区域 ID（含继承）
     */
    Long getZoneByDeptId(Long deptId);

    /**
     * 获取用户今日打卡次数
     */
    int getTodayClockCount(Long userId);
}
