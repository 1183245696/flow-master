package com.flow.org.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flow.org.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 递归 CTE 向上追溯祖先，按 level 降序找第一个非 null 的 attendance_zone_id
     * 支持 MySQL 8.0+ WITH RECURSIVE
     */
    @Select("""
            WITH RECURSIVE ancestors AS (
                SELECT id, parent_id, attendance_zone_id, level
                FROM sys_dept
                WHERE id = #{deptId} AND is_deleted = 0
                UNION ALL
                SELECT d.id, d.parent_id, d.attendance_zone_id, d.level
                FROM sys_dept d
                INNER JOIN ancestors a ON d.id = a.parent_id
                WHERE d.is_deleted = 0
            )
            SELECT attendance_zone_id
            FROM ancestors
            WHERE attendance_zone_id IS NOT NULL
            ORDER BY level DESC
            LIMIT 1
            """)
    Long findInheritedAttendanceZoneId(@Param("deptId") Long deptId);

    /**
     * 获取部门全路径信息（用于 export）
     */
    @Select("""
            SELECT d.name as name, d.path as path, d.level as level,
                   d.attendance_zone_id, d.rule_id, d.leader_user_id
            FROM sys_dept d
            WHERE d.is_deleted = 0
            ORDER BY d.path
            """)
    List<SysDept> selectAllForExport();
}
