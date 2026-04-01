package com.flow.org.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flow.base.response.R;
import com.flow.org.entity.SysPosition;
import com.flow.org.service.MenuService;
import com.flow.org.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "职位管理")
@RestController
@RequestMapping("/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;
    private final MenuService menuService;

    @Operation(summary = "职位列表（按部门分页）")
    @GetMapping
    public R<Page<SysPosition>> page(
            @RequestParam(required = false) Long deptId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(positionService.pageByDept(deptId, pageNum, pageSize));
    }

    @Operation(summary = "新增职位")
    @PostMapping
    public R<SysPosition> create(@RequestBody SysPosition position) {
        return R.ok(positionService.createPosition(position));
    }

    @Operation(summary = "修改职位")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody SysPosition position) {
        position.setId(id);
        positionService.updateById(position);
        return R.ok();
    }

    @Operation(summary = "删除职位")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        positionService.deletePosition(id);
        return R.ok();
    }

    @Operation(summary = "为职位分配菜单权限")
    @PostMapping("/{id}/menus")
    public R<Void> assignMenus(@PathVariable Long id,
                                @RequestBody AssignMenusRequest req) {
        menuService.assignMenusToPosition(id, req.getMenuIds());
        return R.ok();
    }

    @Data
    public static class AssignMenusRequest {
        private List<Long> menuIds;
    }
}
