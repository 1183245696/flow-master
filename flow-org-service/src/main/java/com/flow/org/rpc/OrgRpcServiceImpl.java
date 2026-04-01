package com.flow.org.rpc;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.org.entity.MenuVO;
import com.flow.org.entity.SysDept;
import com.flow.org.entity.SysPosition;
import com.flow.org.mapper.SysPositionMapper;
import com.flow.org.service.DeptService;
import com.flow.org.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@Slf4j
@DubboService(version = "1.0.0", group = "oaplatform")
@RequiredArgsConstructor
public class OrgRpcServiceImpl implements OrgRpcService {

    private final DeptService deptService;
    private final MenuService menuService;
    private final SysPositionMapper positionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public String getDeptById(Long deptId) {
        SysDept dept = deptService.getById(deptId);
        if (dept == null) return null;
        try {
            return objectMapper.writeValueAsString(dept);
        } catch (Exception e) {
            log.error("Serialize dept failed", e);
            return null;
        }
    }

    @Override
    public Long getAttendanceZoneByDeptId(Long deptId) {
        return deptService.getInheritedAttendanceZone(deptId);
    }

    @Override
    public String getMenusByPositionId(Long positionId) {
        List<MenuVO> menus = menuService.treeByPosition(positionId);
        try {
            return objectMapper.writeValueAsString(menus);
        } catch (Exception e) {
            log.error("Serialize menus failed", e);
            return "[]";
        }
    }

    @Override
    public Long getUserPositionInDept(Long userId, Long deptId) {
        // Query position by userId (from sys_user) within deptId
        // Simplified: return first position in that dept belonging to user
        SysPosition pos = positionMapper.selectOne(
                new LambdaQueryWrapper<SysPosition>()
                        .eq(SysPosition::getDeptId, deptId)
                        .last("LIMIT 1"));
        return pos != null ? pos.getId() : null;
    }
}
