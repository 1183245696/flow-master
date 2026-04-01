package com.flow.gateway.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flow.base.response.R;
import com.flow.gateway.entity.BlackWhiteListEntity;
import com.flow.gateway.service.BlackWhiteListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 黑白名单管理接口 — 暴露在 management port 8081
 * POST   /manage/blacklist
 * DELETE /manage/blacklist/{id}
 * GET    /manage/blacklist
 * POST   /manage/whitelist
 * DELETE /manage/whitelist/{id}
 * GET    /manage/whitelist
 */
@Tag(name = "黑白名单管理")
@RestController
@RequestMapping("/manage")
@RequiredArgsConstructor
public class BlackWhiteListController {

    private final BlackWhiteListService service;

    // ── Blacklist ──────────────────────────────────────────────────

    @Operation(summary = "添加黑名单")
    @PostMapping("/blacklist")
    public R<BlackWhiteListEntity> addBlacklist(@RequestBody BlackWhiteListEntity entity) {
        entity.setListType("blacklist");
        return R.ok(service.addEntry(entity));
    }

    @Operation(summary = "删除黑名单")
    @DeleteMapping("/blacklist/{id}")
    public R<Void> deleteBlacklist(@PathVariable Long id) {
        service.removeEntry(id);
        return R.ok();
    }

    @Operation(summary = "查询黑名单列表")
    @GetMapping("/blacklist")
    public R<Page<BlackWhiteListEntity>> listBlacklist(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(service.pageByType("blacklist", page, size));
    }

    // ── Whitelist ──────────────────────────────────────────────────

    @Operation(summary = "添加白名单")
    @PostMapping("/whitelist")
    public R<BlackWhiteListEntity> addWhitelist(@RequestBody BlackWhiteListEntity entity) {
        entity.setListType("whitelist");
        return R.ok(service.addEntry(entity));
    }

    @Operation(summary = "删除白名单")
    @DeleteMapping("/whitelist/{id}")
    public R<Void> deleteWhitelist(@PathVariable Long id) {
        service.removeEntry(id);
        return R.ok();
    }

    @Operation(summary = "查询白名单列表")
    @GetMapping("/whitelist")
    public R<Page<BlackWhiteListEntity>> listWhitelist(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(service.pageByType("whitelist", page, size));
    }
}
