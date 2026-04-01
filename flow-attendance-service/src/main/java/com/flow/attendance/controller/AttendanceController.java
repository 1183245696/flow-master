package com.flow.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flow.attendance.entity.AttRecord;
import com.flow.attendance.entity.AttRule;
import com.flow.attendance.entity.AttZone;
import com.flow.attendance.service.AttendanceService;
import com.flow.base.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "考勤管理")
@RestController
@RequestMapping("/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ── 考勤区域 CRUD ─────────────────────────────────────────────────────────

    @Operation(summary = "考勤区域列表")
    @GetMapping("/zones")
    public R<List<AttZone>> listZones() {
        return R.ok(attendanceService.listZones());
    }

    @Operation(summary = "新增考勤区域")
    @PostMapping("/zones")
    public R<AttZone> createZone(@RequestBody AttZone zone) {
        return R.ok(attendanceService.createZone(zone));
    }

    @Operation(summary = "修改考勤区域")
    @PutMapping("/zones/{id}")
    public R<Void> updateZone(@PathVariable Long id, @RequestBody AttZone zone) {
        zone.setId(id);
        attendanceService.updateZone(zone);
        return R.ok();
    }

    @Operation(summary = "删除考勤区域")
    @DeleteMapping("/zones/{id}")
    public R<Void> deleteZone(@PathVariable Long id) {
        attendanceService.deleteZone(id);
        return R.ok();
    }

    // ── 打卡规则 CRUD ─────────────────────────────────────────────────────────

    @Operation(summary = "打卡规则列表")
    @GetMapping("/rules")
    public R<List<AttRule>> listRules() {
        return R.ok(attendanceService.listRules());
    }

    @Operation(summary = "新增打卡规则")
    @PostMapping("/rules")
    public R<AttRule> createRule(@RequestBody AttRule rule) {
        return R.ok(attendanceService.createRule(rule));
    }

    @Operation(summary = "修改打卡规则")
    @PutMapping("/rules/{id}")
    public R<Void> updateRule(@PathVariable Long id, @RequestBody AttRule rule) {
        rule.setId(id);
        attendanceService.updateRule(rule);
        return R.ok();
    }

    @Operation(summary = "删除打卡规则")
    @DeleteMapping("/rules/{id}")
    public R<Void> deleteRule(@PathVariable Long id) {
        attendanceService.deleteRule(id);
        return R.ok();
    }

    // ── 打卡 ─────────────────────────────────────────────────────────────────

    @Operation(summary = "打卡（上班/下班）")
    @PostMapping("/clock")
    public R<AttRecord> clock(@RequestBody ClockRequest req,
                               @RequestHeader("X-User-Id") Long userId) {
        return R.ok(attendanceService.clockIn(userId, req.getDeptId(),
                req.getLat(), req.getLng(), req.getType()));
    }

    // ── 记录查询 ──────────────────────────────────────────────────────────────

    @Operation(summary = "我的打卡记录（分页）")
    @GetMapping("/records/my")
    public R<Page<AttRecord>> myRecords(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(attendanceService.myRecords(userId, from, to, pageNum, pageSize));
    }

    @Operation(summary = "所有员工打卡记录（管理员）")
    @GetMapping("/records")
    public R<Page<AttRecord>> allRecords(
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(attendanceService.allRecords(deptId, userId, from, to, pageNum, pageSize));
    }

    @Data
    public static class ClockRequest {
        private Long deptId;
        private Double lat;
        private Double lng;
        /** IN | OUT */
        private String type;
    }
}
