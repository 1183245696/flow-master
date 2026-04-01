package com.user.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flow.base.response.R;
import com.user.user.entity.SysUser;
import com.user.user.entity.SysUserTransferHistory;
import com.user.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @GetMapping
    public R<Page<SysUser>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status) {
        return R.ok(userService.pageUsers(pageNum, pageSize, keyword, deptId, status));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public R<SysUser> getById(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public R<SysUser> create(@Valid @RequestBody SysUser user) {
        return R.ok(userService.createUser(user));
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}")
    public R<SysUser> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        return R.ok(userService.updateUser(user));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.ok();
    }

    @Operation(summary = "员工调动")
    @PostMapping("/{userId}/transfer")
    public R<Void> transfer(@PathVariable Long userId,
                             @RequestBody TransferRequest req,
                             @RequestHeader("X-User-Id") Long operatorId) {
        userService.transferUser(userId, req.getToDeptId(), req.getToPositionId(),
                req.getReason(), operatorId);
        return R.ok();
    }

    @Operation(summary = "调动记录分页")
    @GetMapping("/{userId}/transfers")
    public R<Page<SysUserTransferHistory>> transferHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(userService.pageTransferHistory(userId, pageNum, pageSize));
    }

    @Data
    public static class TransferRequest {
        private Long toDeptId;
        private Long toPositionId;
        private String reason;
    }
}
