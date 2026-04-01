package com.flow.org.rpc;

/**
 * Org RPC 接口 — 供 Gateway / Workflow / Attendance 调用
 */
public interface OrgRpcService {

    /** 根据 deptId 获取部门信息（JSON） */
    String getDeptById(Long deptId);

    /**
     * 获取部门的考勤区域 ID（含祖先继承逻辑）
     * 若当前部门 attendanceZoneId=null 则向上递归查找
     */
    Long getAttendanceZoneByDeptId(Long deptId);

    /** 根据 positionId 获取菜单/按钮权限列表（JSON 数组） */
    String getMenusByPositionId(Long positionId);

    /** 获取用户在指定部门的职位 ID */
    Long getUserPositionInDept(Long userId, Long deptId);
}
