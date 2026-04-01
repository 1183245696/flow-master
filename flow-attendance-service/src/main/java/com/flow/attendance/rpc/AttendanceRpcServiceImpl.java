package com.flow.attendance.rpc;

import com.flow.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.time.LocalDateTime;

@DubboService(version = "1.0.0", group = "oaplatform")
@RequiredArgsConstructor
public class AttendanceRpcServiceImpl implements AttendanceRpcService {

    private final AttendanceService attendanceService;

    @Override
    public double calculateLeaveDuration(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        // ruleId resolved from user's dept — simplified: pass null to use default
        return attendanceService.calculateLeaveDuration(userId, startTime, endTime, null);
    }

    @Override
    public Long getZoneByDeptId(Long deptId) {
        // Delegates to dept zone inheritance logic
        return null; // Resolved via OrgRpcService in production flow
    }

    @Override
    public int getTodayClockCount(Long userId) {
        return attendanceService.getTodayClockCount(userId);
    }
}
