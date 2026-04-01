package com.flow.org.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flow.org.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("""
            SELECT m.*
            FROM sys_menu m
            JOIN sys_position_menu pm ON pm.menu_id = m.id
            WHERE pm.position_id = #{positionId}
              AND m.is_deleted = 0
            ORDER BY m.sort
            """)
    List<SysMenu> selectByPositionId(@Param("positionId") Long positionId);
}
