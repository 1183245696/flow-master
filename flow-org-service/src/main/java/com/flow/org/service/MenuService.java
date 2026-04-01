package com.flow.org.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.org.entity.MenuVO;
import com.flow.org.entity.SysMenu;
import com.flow.org.entity.SysPositionMenu;
import com.flow.org.mapper.SysMenuMapper;
import com.flow.org.mapper.SysPositionMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService extends ServiceImpl<SysMenuMapper, SysMenu> {

    private final SysMenuMapper menuMapper;
    private final SysPositionMenuMapper positionMenuMapper;

    /** Full menu tree (all menus) */
    public List<MenuVO> fullTree() {
        List<SysMenu> all = list(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort));
        return buildTree(0L, all);
    }

    /** Menu tree for a specific position (with button perms) */
    public List<MenuVO> treeByPosition(Long positionId) {
        List<SysMenu> perms = menuMapper.selectByPositionId(positionId);
        return buildTree(0L, perms);
    }

    private List<MenuVO> buildTree(Long parentId, List<SysMenu> all) {
        return all.stream()
                .filter(m -> Objects.equals(m.getParentId(), parentId))
                .map(m -> {
                    MenuVO vo = toVO(m);
                    vo.setChildren(buildTree(m.getId(), all));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private MenuVO toVO(SysMenu m) {
        MenuVO vo = new MenuVO();
        vo.setId(m.getId());
        vo.setName(m.getName());
        vo.setPath(m.getPath());
        vo.setComponent(m.getComponent());
        vo.setIcon(m.getIcon());
        vo.setType(m.getType());
        vo.setParentId(m.getParentId());
        vo.setSort(m.getSort());
        vo.setMethod(m.getMethod());
        return vo;
    }

    /** Assign menus to position (replace all) */
    @Transactional
    public void assignMenusToPosition(Long positionId, List<Long> menuIds) {
        // Remove existing
        positionMenuMapper.delete(new LambdaQueryWrapper<SysPositionMenu>()
                .eq(SysPositionMenu::getPositionId, positionId));
        // Insert new
        List<SysPositionMenu> batch = new ArrayList<>();
        for (Long menuId : menuIds) {
            SysPositionMenu pm = new SysPositionMenu();
            pm.setPositionId(positionId);
            pm.setMenuId(menuId);
            batch.add(pm);
        }
        if (!batch.isEmpty()) {
            batch.forEach(positionMenuMapper::insert);
        }
    }
}
