package com.flow.org.controller;

import com.flow.base.response.R;
import com.flow.org.entity.MenuVO;
import com.flow.org.entity.SysMenu;
import com.flow.org.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "完整菜单树")
    @GetMapping("/tree")
    public R<List<MenuVO>> tree() {
        return R.ok(menuService.fullTree());
    }

    @Operation(summary = "新增菜单/按钮")
    @PostMapping
    public R<SysMenu> create(@RequestBody SysMenu menu) {
        menuService.save(menu);
        return R.ok(menu);
    }

    @Operation(summary = "修改菜单")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody SysMenu menu) {
        menu.setId(id);
        menuService.updateById(menu);
        return R.ok();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        menuService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "按职位获取菜单树")
    @GetMapping("/by-position/{positionId}")
    public R<List<MenuVO>> byPosition(@PathVariable Long positionId) {
        return R.ok(menuService.treeByPosition(positionId));
    }
}
