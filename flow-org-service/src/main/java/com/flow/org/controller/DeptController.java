package com.flow.org.controller;

import com.flow.base.response.R;
import com.flow.org.entity.SysDept;
import com.flow.org.service.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/v1/depts")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @Operation(summary = "部门树")
    @GetMapping("/tree")
    public R<List<SysDept>> tree() {
        return R.ok(deptService.buildTree());
    }

    @Operation(summary = "新增部门")
    @PostMapping
    public R<SysDept> create(@RequestBody SysDept dept) {
        return R.ok(deptService.createDept(dept));
    }

    @Operation(summary = "修改部门")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody SysDept dept) {
        dept.setId(id);
        deptService.updateById(dept);
        return R.ok();
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deptService.deleteDept(id);
        return R.ok();
    }

    @Operation(summary = "获取部门考勤区域（含继承）")
    @GetMapping("/{id}/attendance-zone")
    public R<Long> attendanceZone(@PathVariable Long id) {
        return R.ok(deptService.getInheritedAttendanceZone(id));
    }

    @Operation(summary = "导出部门列表 Excel")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        deptService.exportDepts(response);
    }
}
